package com.dx.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.dx.common.Constant;
import com.dx.pojo.vo.CreateOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApiService {

    @Value("${order.url}")
    private String orderUrl;

    public void createOrder(CreateOrderVO vo){

        String body = HttpRequest.post(orderUrl + Constant.OrderUrl.createOrder).body(JSON.toJSONString(vo)).execute().body();
        log.info("创建充值订单返回参数:{}",body);
    }
}
