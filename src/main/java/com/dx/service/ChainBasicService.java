package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.dx.service.other.HttpSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    public JSONObject createAddressBynum(String netName,Integer num){
        return httpSerive.createAddressBynum(netName,num);
    }
    public Integer getnowblock(String netName){
        return httpSerive.getnowblock(netName);
    }
    public String getblockbynum(String netName,Integer num){
        return httpSerive.getblockbynum(netName,num);
    }

    public JSONObject gettransactioninfo(String netName,String txId ){
        return httpSerive.gettransactioninfo(netName,txId);
    }

    public String estimateenergy(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount){
        return httpSerive.estimateenergy(netName,formAddress,toaddress,privateKey,coinCode,amount);
    }

    public String transferContractCoins(String netName, String formAddress, String toaddress, String privateKey, String coinCode, BigDecimal amount){
        return httpSerive.transferContractCoins(netName,formAddress,toaddress,privateKey,coinCode,amount);
    }

    public String transferBaseCoins(String netName, String formAddress, String toaddress, String privateKey, BigDecimal amount){
        return httpSerive.transferBaseCoins(netName,formAddress,toaddress,privateKey,amount);
    }

    public BigDecimal queryBalance(String netName,String address){
        return httpSerive.queryBalance(netName,address);
    }

    public BigDecimal queryContractBalance(String netName,String coinCode,String address){
        return httpSerive.queryContractBalance(netName,coinCode,address);
    }
}
