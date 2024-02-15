package com.dx.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dx.common.Constant;
import com.dx.common.Result;
import com.dx.pojo.dto.GetCurrencyListDTO;
import com.dx.pojo.vo.CreateOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ApiService {

    @Value("${order.url}")
    private String orderUrl;
    @Value("${trade.url}")
    private String tradeUrl;

    public void createOrder(CreateOrderVO vo){

        String body = HttpRequest.post(orderUrl + Constant.OrderUrl.createOrder).body(JSON.toJSONString(vo)).execute().body();
        log.info("创建充值订单返回参数:{}",body);
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
