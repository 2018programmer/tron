package com.dx;

import com.dx.futures.trade.CryptoContractTrading;
import com.dx.futures.trade.Order;
import com.dx.futures.trade.Trade;
import com.dx.futures.trade.TradeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.dx.futures.trade.CryptoContractTrading.*;


@RestController
@RequestMapping("/test")
public class TestexController {

    @Autowired
    private CryptoContractTrading trading;


    @GetMapping("")
    public void  getHotwalletBalance( ){
        trading.addTradeListener(new TradeListener() {
            @Override
            public void onTradeExecuted(Trade trade) {
                System.out.println("交易成功："+trade.toString());
            }
        });
        testOrderBookAndStopOut(trading);
    }

    public   void testOrderBookAndStopOut(CryptoContractTrading cryptoContractTrading) {
        String symbol = "BTC/USDT";

        // 创建 5 个买单
        for (int i = 0; i < 5; i++) {
            Order buyOrder = new Order(symbol, CryptoContractTrading.ORDER_TYPE_BUY, 1.0, 51500.0 - i * 100, ORDER_STATUS_OPEN, false);
            cryptoContractTrading.placeOrder(buyOrder);
        }

        // 创建 10 个卖单
        for (int i = 0; i < 10; i++) {
            Order sellOrder = new Order(symbol, CryptoContractTrading.ORDER_TYPE_SELL, 1.0, 52500.0 + i * 100, ORDER_STATUS_OPEN, false);
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
