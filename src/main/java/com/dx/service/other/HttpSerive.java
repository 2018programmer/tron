package com.dx.service.other;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dx.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class HttpSerive {

    @Value("${base.url}")
    private String chainbaseUrl;

    /**
     * 生成地址
     */
    public JSONObject createAddress(String netName){
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.CREATEADDRESS).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("生成地址失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");

        return result;
    }
    public JSONObject createAddressBynum(String netName,Integer num){
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.CREATEADDRESSBYNUM+"/"+num).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("生成地址失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");

        return result;
    }
    public Integer getnowblock(String netName) {

        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETNOWBLOCK).execute().body();
        log.info("查询最新区块，查询结果:{}",body);
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块失败");
        }
        Integer result = jsonObject.getInteger("result");

        return result;
    }

    public String getblockbynum(String netName,long num) {
        Map<String, Object> map = new HashMap<>();
        map.put("num",num);
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETBLOCKBYNUM).form(map).execute().body();
        log.info("查询第{}区块，查询结果:{}",num,body);
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块信息失败");
        }
        JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("data");
        if(CollectionUtils.isEmpty(jsonArray)){
            return null;
        }
        return jsonArray.toJSONString();
    }

    public String  transferContractCoins(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toAddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.TRANSFERCONTRACTCOINS+"/"+coinCode).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取交易需要矿工费失败");
        }
        String  result = jsonObject.getJSONObject("result").getString("txId");;
        return result;
    }
    public String  estimateenergy(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toAddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.ESTIMATEENERGY+"/"+coinCode).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("请求交易失败");
        }
        String result = jsonObject.getString("result");
        return result;
    }

    public String  transferBaseCoins(String netName, String formAddress, String toaddress, String privateKey, BigDecimal amount) {

        JSONObject req = new JSONObject();
        req.put("formAddress",formAddress);
        req.put("toAddress",toaddress);
        req.put("privateKey",privateKey);
        req.put("amount",amount);

        String body = HttpRequest.post(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.TRANSFERBASECOINS).body(req.toJSONString()).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            return null;
        }
        String result = jsonObject.getJSONObject("result").getString("txId");
        return result;
    }

    public JSONObject gettransactioninfo(String netName,String txId) {
        Map<String, Object> map = new HashMap<>();
        map.put("txId",txId);
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.GETTRANSACTIONINFO).form(map).execute().body();
        log.info("查询交易信息txid:{}，查询结果:{}",txId,body);
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            log.info("获取区块信息失败");
            return new JSONObject();
        }
        JSONObject result = jsonObject.getJSONObject("result");
        return result;
    }
    public BigDecimal queryBalance(String netName,String address){
        Map<String, Object> map = new HashMap<>();
        map.put("address",address);
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.QUERYBASEBALANCE).form(map).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块信息失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        if(result.isEmpty()){
            return BigDecimal.ZERO;
        }
        return new BigDecimal(result.getString("balance"));
    }

    public BigDecimal queryContractBalance(String netName,String coinCode,String address){
        Map<String, Object> map = new HashMap<>();
        map.put("address",address);
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.QUERYCONTRACTBALANCE+"/"+coinCode).form(map).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            throw new RuntimeException("获取区块信息失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        if(result.isEmpty()){
            return BigDecimal.ZERO;
        }
        return new BigDecimal(result.getString("balance"));
    }

    public Boolean verifyAddress(String netName, String address) {
        Map<String, Object> map = new HashMap<>();
        map.put("address",address);
        String body = HttpRequest.get(chainbaseUrl + Constant.BaseUrl.V1_CHAIN + netName + Constant.BaseUrl.VERIFYADDRESS).form(map).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        Boolean success = jsonObject.getBoolean("success");
        if(Objects.isNull(success)||false==success){
            return false;
        }
        return jsonObject.getBoolean("result");
    }
}
