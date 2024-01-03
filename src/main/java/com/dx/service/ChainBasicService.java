package com.dx.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dx.common.Constant;
import com.dx.service.other.HttpSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 调用
 */
@Service
public class ChainBasicService {

    @Autowired
    private HttpSerive httpSerive;
    /**
     * 生成地址
     */
    public JSONObject createAddress(String netName){
        return httpSerive.createAddress(netName);
    }
}
