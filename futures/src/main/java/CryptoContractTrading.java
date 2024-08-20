import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CryptoContractTrading {
    private static final String ORDER_TYPE_BUY = "BUY";
    private static final String ORDER_TYPE_SELL = "SELL";
    private static final String ORDER_STATUS_OPEN = "OPEN";
    private static final String ORDER_STATUS_FILLED = "FILLED";
    private static final String ORDER_STATUS_PARTIALLY_FILLED = "PARTIALLY_FILLED";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;

    public CryptoContractTrading(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }



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
    }



    private void recordTrade(String symbol, double amount, double price, String buyOrderId, String sellOrderId) {
        String tradeId = UUID.randomUUID().toString();
        Map<String, Object> tradeInfo = new HashMap<>();
        tradeInfo.put("symbol", symbol);
        tradeInfo.put("amount", amount);
        tradeInfo.put("price", price);
        tradeInfo.put("buyOrderId", buyOrderId);
        tradeInfo.put("sellOrderId", sellOrderId);
        tradeInfo.put("timestamp", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll("trade:" + tradeId, tradeInfo);
        redisTemplate.opsForList().leftPush("tradeHistory:" + symbol, tradeId);
    }

    private Order getOrderFromRedis(String orderId) {
        Map<Object, Object> orderMap = redisTemplate.opsForHash().entries("order:" + orderId);
        return convertMapToOrder(orderMap);
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
        return order;
    }

    public Map<Object, Object> getOrderInfo(String orderId) {
        return redisTemplate.opsForHash().entries("order:" + orderId);
    }

    public List<Object> getTradeHistory(String symbol, int limit) {
        return redisTemplate.opsForList().range("tradeHistory:" + symbol, 0, limit - 1);
    }

    public void shutdown() {
        executorService.shutdown();
    }
    public String placeOrder(Order order) {
        String orderId = UUID.randomUUID().toString();
        order.setId(orderId);
        order.setTimestamp(System.currentTimeMillis());

        String key = "order:" + orderId;
        redisTemplate.opsForHash().putAll(key, convertOrderToMap(order));

        String orderQueue = order.getType().equals(ORDER_TYPE_BUY) ? "buyOrders:" : "sellOrders:";
        redisTemplate.opsForZSet().add(orderQueue + order.getSymbol(), orderId, order.getPrice());

        executorService.submit(() -> matchOrders(order.getSymbol()));

        return orderId;
    }

    private void updateOrderAfterTrade(Order order, double tradedAmount) {
        order.setAmount(order.getAmount() - tradedAmount);
        order.setStatus(order.getAmount() == 0 ? ORDER_STATUS_FILLED : ORDER_STATUS_PARTIALLY_FILLED);
        redisTemplate.opsForHash().putAll("order:" + order.getId(), convertOrderToMap(order));
    }

    public Map<String, Double> calculatePotentialProfit(String orderId, double currentPrice) {
        Order order = getOrderFromRedis(orderId);
        if (order == null || order.getStatus().equals(ORDER_STATUS_FILLED)) {
            return null;
        }

        double remainingAmount = order.getAmount();
        double potentialProfit = 0;

        if (order.getType().equals(ORDER_TYPE_BUY)) {
            potentialProfit = (currentPrice - order.getPrice()) * remainingAmount;
        } else if (order.getType().equals(ORDER_TYPE_SELL)) {
            potentialProfit = (order.getPrice() - currentPrice) * remainingAmount;
        }

        Map<String, Double> result = new HashMap<>();
        result.put("potentialProfit", potentialProfit);
        result.put("remainingAmount", remainingAmount);

        return result;
    }
}