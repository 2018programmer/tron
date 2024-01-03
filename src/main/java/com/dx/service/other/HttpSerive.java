package com.dx.service.other;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dx.common.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HttpSerive {

    @Value("${base.url}")
    private String url;
    /**
     * 生成地址
     */
    public JSONObject createAddress(String netName){
        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.CREATEADDRESS).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("生成地址失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");

        return result;
    }
}
