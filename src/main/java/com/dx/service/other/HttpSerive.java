package com.dx.service.other;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dx.common.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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
    public JSONObject createAddressBynum(String netName,Integer num){
        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.CREATEADDRESSBYNUM+"/"+num).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("生成地址失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");

        return result;
    }
    public Integer getnowblock(String netName) {

        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETNOWBLOCK).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块失败");
        }
        Integer result = jsonObject.getInteger("result");

        return result;
    }

    public JSONObject getblockbynum(String netName,Integer num) {
        Map<String, Object> map = new HashMap<>();
        map.put("num",num);
        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETBLOCKBYNUM).form(map).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块信息失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        return result;
    }

    public String  transferContractCoins(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toaddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.ESTIMATEENERGY+"/"+coinCode).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取交易需要矿工费失败");
        }
        String  result = jsonObject.getString("result");
        return result;
    }
    public String  estimateenergy(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toaddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.TRANSFERCONTRACTCOINS+"/"+coinCode).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("请求交易失败");
        }
        String result = jsonObject.getJSONObject("result").getString("txId");
        return result;
    }

    public String  transferBaseCoins(String netName, String formAddress, String toaddress, String privateKey, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toaddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.TRANSFERBASECOINS).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("请求交易失败");
        }
        String result = jsonObject.getJSONObject("result").getString("txId");
        return result;
    }

    public JSONObject gettransactioninfo(String netName,String txId) {
        Map<String, Object> map = new HashMap<>();
        map.put("txId",txId);
        String body = HttpRequest.get(url + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETTRANSACTIONINFO).form(map).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块信息失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        return result;
    }
}
