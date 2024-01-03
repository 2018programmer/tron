package com.dx.service;

import com.dx.common.Result;
import com.dx.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ChainPoolAddressService {
    /**
     * 生成地址
     */
    public void  createAddress(){

        //调用基础服务生成地址


        //保存地址
    }

    public Result<List<CoinManageDTO>> getPoolManage(String netName) {
        Result<List<CoinManageDTO>> result = new Result<>();
        CoinManageDTO coinManageDTO = new CoinManageDTO();
        coinManageDTO.setCoinName("TRX");
        coinManageDTO.setThreshold(BigDecimal.TEN);
        coinManageDTO.setAutoGather(0);
        coinManageDTO.setTotalBalance(BigDecimal.ZERO);
        coinManageDTO.setCoinCode("TRX");
        List<CoinManageDTO> coinlist = new ArrayList<>();
        coinlist.add(coinManageDTO);
        result.setResult(coinlist);
        return result;
    }

    public Result updatePoolManage(UpdatePoolManageDTO dto) {
        Result<Object> result = new Result<>();
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<PoolManageDTO>> getNets() {
        Result<List<PoolManageDTO>> result = new Result<>();
        PoolManageDTO poolManageDTO = new PoolManageDTO();
        poolManageDTO.setNetName("TRON");
        poolManageDTO.setTotalNum(0);
        poolManageDTO.setGatherStatus(0);
        poolManageDTO.setNoAssignedNum(0);
        List<PoolManageDTO> poollist = new ArrayList<>();
        poollist.add(poolManageDTO);
        result.setResult(poollist);
        return  result;
    }

    public Result<List<PoolAddressDTO>> getPoolAddress(QueryPoolAddressDTO dto) {
        Result<List<PoolAddressDTO>> result = new Result<>();
        PoolAddressDTO poolAddressDTO = new PoolAddressDTO();
        poolAddressDTO.setAddress("18mGmSB357AnGko9C1dfXJJCmRFCM9");
        poolAddressDTO.setCreateTime(new Date());
        poolAddressDTO.setIsAssigned(0);
        poolAddressDTO.setIsActivated(0);
        poolAddressDTO.setAssignType(0);
        poolAddressDTO.setAssignId(null);
        poolAddressDTO.setEstimateBalance(BigDecimal.ZERO);
        List<PoolAddressDTO> list = new ArrayList<>();
        list.add(poolAddressDTO);

        result.setResult(list);

        return result;
    }
}
