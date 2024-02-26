package com.dx.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.pojo.dto.GetCurrencyListDTO;
import com.dx.pojo.vo.CreateOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ApiService {

    @Value("${order.url}")
    private String orderUrl;
    @Value("${trade.url}")
    private String tradeUrl;

    public String createOrder(CreateOrderVO vo){

        String body = HttpRequest.post(orderUrl + Constant.OrderUrl.CREATEORDER).body(JSON.toJSONString(vo)).execute().body();
        log.info("创建充值订单返回参数:{}",body);
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if (Objects.isNull(success) || false == success) {
            return null;
        }
        return jsonObject.getJSONObject("result").getString("orderId");
    }

    public Map<String, String> getPriceList(){
        JSONObject req = new JSONObject();
        req.put("priceCurrency","CNY");
        req.put("type",1);
        req.put("pageSize",200);
        String body = HttpRequest.post(orderUrl + Constant.OrderUrl.GETPRICELIST).body(req.toJSONString()).execute().body();
        log.info("获取交易对配置返回参数:{}",body);
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if (Objects.isNull(success) || false == success) {
            return null;
        }

        JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("records");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i <jsonArray.size(); i++) {
            JSONObject res = jsonArray.getJSONObject(i);
            map.put(res.getString("targetCurrency"),res.getString("buyOne").replace("CNY",""));
        }
        return map;
    }

    public List<GetCurrencyListDTO> getCurrencyList(){
        JSONObject req = new JSONObject();
        req.put("type",2);
        String body = HttpRequest.post(tradeUrl + Constant.TradeUrl.GETCURRENCYLIST).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if (Objects.isNull(success) || false == success) {

            return new ArrayList<>();
        }
        Result<List<GetCurrencyListDTO>> result = JSON.parseObject(body, new TypeReference<Result<List<GetCurrencyListDTO>>>() {
        });
        return result.getResult();
    }
}
