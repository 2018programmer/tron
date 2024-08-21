import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public class Main {

    @Autowired
    public static  RedisTemplate<String,Object> redisTemplate = null ;

    public static void main(String[] args) {
        systemTradable字段有了，但是交易时候代码没有修改。systemTradable==true的时候如果对手方也是systemtrade 那么两者不应该产生交易。第二 ：爆仓价格设计的不对，应该order加个张数字段。getStopOutPrice直接传递order对象。 爆仓价格计算规则是 1. 多头仓位（Long Position）爆仓价格计算公式：

        爆仓价格=开仓价格×(1−1杠杆倍数)\text{爆仓价格} = \text{开仓价格} \times \left( 1 - \frac{1}{\text{杠杆倍数}} \right)爆仓价格=开仓价格×(1−杠杆倍数1​)

        2. 空头仓位（Short Position）爆仓价格计算公式：

        爆仓价格=开仓价格×(1+1杠杆倍数)\text{爆仓价格} = \text{开仓价格} \times \left( 1 + \frac{1}{\text{杠杆倍数}} \right)爆仓价格=开仓价格×(1+杠杆倍数1​)
        CryptoContractTrading cryptoContractTrading = new CryptoContractTrading(redisTemplate);
         testOrderBookAndStopOut(cryptoContractTrading);
    }

    /**
     * 测试订单簿和爆仓价格
     */
    public static void testOrderBookAndStopOut(CryptoContractTrading cryptoContractTrading) {
        String symbol = "BTC/USDT";

        // 创建 5 个买单
        for (int i = 0; i < 5; i++) {
            Order buyOrder = new Order(symbol, CryptoContractTrading.ORDER_TYPE_BUY, 1.0, 50000.0 - i * 100, ORDER_STATUS_OPEN, true);
            cryptoContractTrading.placeOrder(buyOrder);
        }

        // 创建 10 个卖单
        for (int i = 0; i < 10; i++) {
            Order sellOrder = new Order(symbol, CryptoContractTrading.ORDER_TYPE_SELL, 1.0, 51000.0 + i * 100, ORDER_STATUS_OPEN, true);
            cryptoContractTrading.placeOrder(sellOrder);
        }

        // 输出订单簿快照
        double[] priceIntervals = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200};
        Map<Double, Double> orderBookSnapshot = cryptoContractTrading.getOrderBookSnapshot(symbol, priceIntervals);

        for (Map.Entry<Double, Double> entry : orderBookSnapshot.entrySet()) {
            System.out.printf("Price Interval: %.2f - %.2f, Total Volume: %.2f%n",
                    entry.getKey(), entry.getKey() + 10, entry.getValue());
        }

        // 测试爆仓价格
        for (int i = 0; i < 5; i++) {
            String buyOrderId = "buyOrder-" + i;
            double stopOutPrice = cryptoContractTrading.getStopOutPrice(buyOrderId);
            System.out.printf("Buy Order %s: Stop Out Price = %.2f%n", buyOrderId, stopOutPrice);
        }
    }
}
