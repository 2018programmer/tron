import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * 持仓金额（Position Size）：合约的总价值，即开仓价格乘以合约数量。=开仓价格×合约数量
 * 初始保证金（Initial Margin） = 持仓金额/杠杆倍数
 * 维持保证金（Maintenance Margin）：通常是一个固定值或百分比。 持仓总价值×10%
 * 开仓价格（Entry Price）：持仓时的市场价格。
 * 
 * 爆仓价格计算：
 * 多：开仓价格 - （（初始保证金-维持保证金）/合约数量） = 100 - （（1000/5-100）/10）= 90
 * 空： 开仓价格*（（初始保证金-维持保证金）/持仓金额）+维持保证金
 *      100 *  （（1000/5-100）/1000=1/10 + 100=110
 *      100 开10张 5倍数 = 110 爆仓 。
 */
@Component
public class CryptoContractTrading {

    // 订单类型常量
    public static final String ORDER_TYPE_BUY = "BUY";
    public static final String ORDER_TYPE_SELL = "SELL";

    // 订单状态常量
    public static final String ORDER_STATUS_OPEN = "OPEN";
    public static final String ORDER_STATUS_FILLED = "FILLED";
    public static final String ORDER_STATUS_PARTIALLY_FILLED = "PARTIALLY_FILLED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;
    private final Map<String, Double> currentPrices = new ConcurrentHashMap<>(); // 记录每个交易对的当前价格
    private Map<String, NavigableMap<Double, Double>> orderBookSnapshots = new ConcurrentHashMap<>();

    private List<TradeListener> tradeListeners = new CopyOnWriteArrayList<>(); // 交易监听器列表

    public CryptoContractTrading(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("symbol", order.getSymbol());
        map.put("type", order.getType());
        map.put("amount", order.getAmount());
        map.put("price", order.getPrice());
        map.put("status", order.getStatus());
        map.put("timestamp", order.getTimestamp());
        map.put("systemTradable", order.isSystemTradable());
        return map;
    }

    private Order convertMapToOrder(Map<Object, Object> map) {
        Order order = new Order();
        order.setId((String) map.get("id"));
        order.setSymbol((String) map.get("symbol"));
        order.setType((String) map.get("type"));
        order.setAmount((Double) map.get("amount"));
        order.setPrice((Double) map.get("price"));
        order.setStatus((String) map.get("status"));
        order.setTimestamp((Long) map.get("timestamp"));
        order.setSystemTradable((Boolean) map.get("systemTradable"));
        return order;
    }

    private Map<String, Object> convertTradeToMap(Trade trade) {
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", trade.getSymbol());
        map.put("amount", trade.getAmount());
        map.put("price", trade.getPrice());
        map.put("buyOrderId", trade.getBuyOrderId());
        map.put("sellOrderId", trade.getSellOrderId());
        map.put("timestamp", trade.getTimestamp());
        return map;
    }

    private Trade convertMapToTrade(Map<Object, Object> map) {
        return new Trade(
                (String) map.get("symbol"),
                (Double) map.get("amount"),
                (Double) map.get("price"),
                (String) map.get("buyOrderId"),
                (String) map.get("sellOrderId"),
                (Long) map.get("timestamp")
        );
    }


    /**
     * 下单
     *
     * @param order 订单对象
     * @return 订单ID
     */
    public String placeOrder(Order order) {
        String orderId = UUID.randomUUID().toString();
        order.setId(orderId);
        order.setTimestamp(System.currentTimeMillis());

        String key = "order:" + orderId;
        redisTemplate.opsForHash().putAll(key, convertOrderToMap(order));

        String orderQueue = order.getType().equals(ORDER_TYPE_BUY) ? "buyOrders:" : "sellOrders:";
        redisTemplate.opsForZSet().add(orderQueue + order.getSymbol(), orderId, order.getPrice());

        executorService.submit(() -> matchOrders(order.getSymbol()));

        updateOrderBookSnapshot(order.getSymbol(), order.getType(), order.getPrice(), order.getAmount());

        return orderId;
    }

    /**
     * 撤销订单
     *
     * @param orderId 订单ID
     */
    public void cancelOrder(String orderId) {
        String key = "order:" + orderId;
        Order order = getOrderFromRedis(orderId);
        if (order != null && order.getStatus().equals(ORDER_STATUS_OPEN)) {
            order.setStatus(ORDER_STATUS_CANCELLED);
            redisTemplate.opsForHash().putAll(key, convertOrderToMap(order));

            String orderQueue = order.getType().equals(ORDER_TYPE_BUY) ? "buyOrders:" : "sellOrders:";
            redisTemplate.opsForZSet().remove(orderQueue + order.getSymbol(), orderId);
        }
    }

    /**
     * 订单簿匹配
     *
     * @param symbol 交易对
     */
    private void matchOrders(String symbol) {
        while (true) {
            Set<Object> topBuyOrder = redisTemplate.opsForZSet().reverseRange("buyOrders:" + symbol, 0, 0);
            Set<Object> topSellOrder = redisTemplate.opsForZSet().range("sellOrders:" + symbol, 0, 0);

            if (topBuyOrder.isEmpty() || topSellOrder.isEmpty()) {
                break;
            }

            Object buyOrderId = topBuyOrder.iterator().next();
            Object sellOrderId = topSellOrder.iterator().next();

            Order buyOrder = getOrderFromRedis(buyOrderId.toString());
            Order sellOrder = getOrderFromRedis(sellOrderId.toString());

            if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                executeTransaction(buyOrder, sellOrder);
            } else {
                break;
            }
        }
    }

    /**
     * 执行交易
     *
     * @param buyOrder  买单
     * @param sellOrder 卖单
     */
    private void executeTransaction(Order buyOrder, Order sellOrder) {
        double executionPrice = (buyOrder.getPrice() + sellOrder.getPrice()) / 2;
        double tradeAmount = Math.min(buyOrder.getAmount(), sellOrder.getAmount());

        updateOrderAfterTrade(buyOrder, tradeAmount);
        updateOrderAfterTrade(sellOrder, tradeAmount);

        recordTrade(buyOrder.getSymbol(), tradeAmount, executionPrice, buyOrder.getId(), sellOrder.getId());

        if (buyOrder.getAmount() == 0) {
            redisTemplate.opsForZSet().remove("buyOrders:" + buyOrder.getSymbol(), buyOrder.getId());
        }
        if (sellOrder.getAmount() == 0) {
            redisTemplate.opsForZSet().remove("sellOrders:" + sellOrder.getSymbol(), sellOrder.getId());
        }

        updateOrderBookSnapshot(buyOrder.getSymbol(), buyOrder.getType(), buyOrder.getPrice(), -tradeAmount);
        updateOrderBookSnapshot(sellOrder.getSymbol(), sellOrder.getType(), sellOrder.getPrice(), tradeAmount);
    }

    /**
     * 更新订单状态
     *
     * @param order        订单
     * @param tradedAmount 已成交数量
     */
    private void updateOrderAfterTrade(Order order, double tradedAmount) {
        order.setAmount(order.getAmount() - tradedAmount);
        order.setStatus(order.getAmount() == 0 ? ORDER_STATUS_FILLED : ORDER_STATUS_PARTIALLY_FILLED);
        redisTemplate.opsForHash().putAll("order:" + order.getId(), convertOrderToMap(order));
    }

    /**
     * 记录交易
     *
     * @param symbol      交易对
     * @param amount      成交数量
     * @param price       成交价格
     * @param buyOrderId  买单ID
     * @param sellOrderId 卖单ID
     */
    private void recordTrade(String symbol, double amount, double price, String buyOrderId, String sellOrderId) {
        String tradeId = UUID.randomUUID().toString();
        Trade trade = new Trade(symbol, amount, price, buyOrderId, sellOrderId, System.currentTimeMillis());
        redisTemplate.opsForHash().putAll("trade:" + tradeId, convertTradeToMap(trade));
        redisTemplate.opsForList().leftPush("tradeHistory:" + symbol, tradeId);
        notifyTradeListeners(trade);
    }

    /**
     * 更新订单簿快照
     *
     * @param symbol 交易对
     * @param type   订单类型（BUY 或 SELL）
     * @param price  价格
     * @param amount 数量（正数表示增加，负数表示减少）
     */
    private void updateOrderBookSnapshot(String symbol, String type, double price, double amount) {
        NavigableMap<Double, Double> snapshot = orderBookSnapshots.computeIfAbsent(symbol, k -> new TreeMap<>((a, b) -> Double.compare(b, a)));
        snapshot.merge(price, amount, (oldValue, newValue) -> oldValue + newValue);
        if (snapshot.get(price) == 0.0) {
            snapshot.remove(price);
        }
    }

    /**
     * 获取订单簿快照
     *
     * @param symbol         交易对
     * @param priceIntervals 价格区间数组
     * @return 订单簿快照信息
     */
    public Map<Double, Double> getOrderBookSnapshot(String symbol, double[] priceIntervals) {
        Map<Double, Double> snapshot = new LinkedHashMap<>();
        NavigableMap<Double, Double> orderBook = orderBookSnapshots.getOrDefault(symbol, new TreeMap<>((a, b) -> Double.compare(b, a)));

        for (double interval : priceIntervals) {
            double volume = 0.0;
            for (Map.Entry<Double, Double> entry : orderBook.subMap(interval, true, Double.MAX_VALUE, true).entrySet()) {
                volume += entry.getValue();
            }
            snapshot.put(interval, volume);
        }

        return snapshot;
    }

    /**
     * 计算爆仓价格
     *
     * @param orderId 订单ID
     * @return 爆仓价格
     */
    public double getStopOutPrice(String orderId) {
        Order order = getOrderFromRedis(orderId);
        if (order == null || order.getType().equals(ORDER_TYPE_SELL)) {
            return 0.0;
        }

        500 /501 => 2.5
        price*amount / current
        double currentPrice = getCurrentPrice(order.getSymbol());
        double stopOutPrice = order.getPrice() * 0.95; // 以当前价格的 95% 作为爆仓价格
        return Math.max(stopOutPrice, currentPrice * 0.95); // 取最高的 95%
    }

    /**
     * 添加交易监听器
     *
     * @param listener 交易监听器
     */
    public void addTradeListener(TradeListener listener) {
        tradeListeners.add(listener);
    }

    /**
     * 移除交易监听器
     *
     * @param listener 交易监听器
     */
    public void removeTradeListener(TradeListener listener) {
        tradeListeners.remove(listener);
    }

    /**
     * 通知交易监听器
     *
     * @param trade 交易对象
     */
    private void notifyTradeListeners(Trade trade) {
        for (TradeListener listener : tradeListeners) {
            listener.onTradeExecuted(trade);
        }
    }

    /**
     * 从 Redis 获取订单
     *
     * @param orderId 订单ID
     * @return 订单对象
     */
    private Order getOrderFromRedis(String orderId) {
        Map<Object, Object> orderMap = redisTemplate.opsForHash().entries("order:" + orderId);
        return convertMapToOrder(orderMap);
    }

    /**
     * 设置交易对的当前价格
     *
     * @param symbol 交易对
     * @param price  当前价格
     */
    public void setCurrentPrice(String symbol, double price) {
        currentPrices.put(symbol, price);
    }

    /**
     * 获取交易对的当前价格
     *
     * @param symbol 交易对
     * @return 当前价格
     */
    public double getCurrentPrice(String symbol) {
        return currentPrices.getOrDefault(symbol, 0.0);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
    }
}