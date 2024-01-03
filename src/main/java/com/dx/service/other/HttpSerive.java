package com.dx.service.other;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dx.common.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HttpSerive {

    @Value("${base.url}")
    private String url;
    /**
     * 生成地址
     */
    public JSONObject createAddress(String netName){
        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.CREATEADDRESS).execute().body();
        return JSON.parseObject(body);
    }
}
