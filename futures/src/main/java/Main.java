import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public class Main {

    @Autowired
    public static  RedisTemplate<String,Object> redisTemplate = null ;

    public static void main(String[] args) {
        // 如下需要整改 然后给出全部完整代码
        // 增加取消订单功能 。
        // 返回的内容 也包装成对象 calculatePotentialProfit(String orderId, double currentPrice) {
        // recordTrade里面的map用对象实现 同时给出字段注释 和 getter setter
        // 需要判断equals==“”的地方 都优化成常量来表示。并且为每个常量注释。
        // 加入一个回调用监听函数 实现可以读取到新交易成功记录的函数 （应该是 redisTemplate.opsForHash().putAll("trade:" + tradeId, tradeInfo);redisTemplate.opsForList().leftPush("tradeHistory:" + symbol, tradeId); 存入的数据 对不对）。返回所有关联到的已知信息
        // 买入/卖出 增加系统级别。（为CryptoContractTrading对象增加一个设定不同symbol的当前价格属性 当订单买入/卖出时候 即便没有对应出售订单 也能完成交易 。同时order 字段增加一个属性 是否可以系统级别交易）。 为Order的全部字段加注释
        // 新增一个函数。功能给出一个价格分段值 和价格。比如10,160（那么如价格130 140 150 170 180 190就是区间） 100（100 200 300），然后返回给出所有挂单的区间对应数量（四舍五入）

        // 模拟 5个买10个卖的测试函数
        // 增加一个函数 输入订单号 返回值是价格。这个价格指的是买入的订单到达爆仓的条件

        String orderId = "someOrderId";
        double currentPrice = 55000.0;
        CryptoContractTrading cryptoContractTrading = new CryptoContractTrading(redisTemplate);
        Map<String, Double> result = cryptoContractTrading.calculatePotentialProfit(orderId, currentPrice);

        if (result != null) {
            System.out.println("Potential Profit: " + result.get("potentialProfit"));
            System.out.println("Remaining Amount: " + result.get("remainingAmount"));
        } else {
            System.out.println("Order not found or already filled");
        }
    }
}
