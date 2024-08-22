package com.dx.futures.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.NavigableMap;
import java.util.TreeMap;

public class Main {

    @Autowired
    public static  RedisTemplate<String,Object> redisTemplate = null ;

    public static void main(String[] args) {
//        systemTradable字段有了，但是交易时候代码没有修改。systemTradable==true的时候如果对手方也是systemtrade 那么两者不应该产生交易。第二 ：爆仓价格设计的不对，应该order加个张数字段。
//        getStopOutPrice直接传递order对象 和 维持金额百分比。 爆仓价格计算规则是
//        持仓金额（Position Size）：合约的总价值，即开仓价格乘以合约数量。=开仓价格×合约数量
//                * 初始保证金（Initial Margin） = 持仓金额/杠杆倍数
//                * 维持保证金（Maintenance Margin）：通常是一个固定值或百分比。 持仓总价值×10%
// * 开仓价格（Entry Price）：持仓时的市场价格。
// *
// * 爆仓价格计算：
// * 多：开仓价格 - （（初始保证金-维持保证金）/合约数量） = 100 - （（1000/5-100）/10）= 90
//                * 空： 开仓价格*（（初始保证金-维持保证金）/持仓金额）+维持保证金
//                *      100 *  （（1000/5-100）/1000=1/10 + 100=110
//                *      100 开10张 5倍数 = 110 爆仓 。
       // CryptoContractTrading cryptoContractTrading = new CryptoContractTrading();
        // testOrderBookAndStopOut(cryptoContractTrading);

        TreeMap<Double, String> map = new TreeMap<>();
        map.put(  Double.valueOf(1.0), "A");
        map.put(Double.valueOf(2.0), "B");
        map.put(Double.valueOf(3.0), "C");

        double fromKey = 0.1;
        double toKey = 0.5;

        try {
            // Using inclusive bounds for fromKey and toKey
            NavigableMap<Double, String> subMap = map.subMap(fromKey, true, toKey, false);
            System.out.println("Submap: " + subMap);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * 测试订单簿和爆仓价格
     */

}
