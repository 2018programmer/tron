package com.dx.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.dx.common.Constant;
import com.dx.vo.CreateOrderVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    @Value("${order.url}")
    private String orderUrl;

    public void createOrder(CreateOrderVO vo){

        HttpRequest.post(orderUrl+ Constant.OrderUrl.createOrder).body(JSON.toJSONString(vo)).execute().body();
    }
}
