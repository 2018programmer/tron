package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.entity.ChainAddressExpenses;
import com.dx.mapper.ChainAddressExpensesMapper;
import com.dx.vo.GetAddressExpensesVO;
import com.dx.vo.GetAddressIncomeVO;
import com.dx.entity.ChainAddressIncome;
import com.dx.mapper.ChainAddressIncomeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChainTranferService {

    @Autowired
    private ChainAddressIncomeMapper addressIncomeMapper;

    @Autowired
    private ChainAddressExpensesMapper addressExpensesMapper;
    @Autowired
    private ChainBasicService basicService;


    public Result<IPage<ChainAddressIncome>> getAddressIncome(GetAddressIncomeVO vo) {
        Result<IPage<ChainAddressIncome>> result = new Result<>();
        LambdaQueryWrapper<ChainAddressIncome> wrapper = Wrappers.lambdaQuery();

        if(StringUtils.isNotEmpty(vo.getAddress())){
            wrapper.eq(ChainAddressIncome::getAddress,vo.getAddress());
        }
        if(StringUtils.isNotEmpty(vo.getSerial())){
            wrapper.eq(ChainAddressIncome::getSerial,vo.getSerial());
        }
        if(StringUtils.isNotEmpty(vo.getCoinName())){
            wrapper.eq(ChainAddressIncome::getCoinName,vo.getCoinName());
        }
        if(ObjectUtils.isNotNull(vo.getEffective())){
            wrapper.eq(ChainAddressIncome::getEffective,vo.getEffective());
        }
        IPage<ChainAddressIncome> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page = addressIncomeMapper.selectPage(page, wrapper);

        result.setResult(page);
        return result;

    }

    public Result<IPage<ChainAddressExpenses>> getAddressExpenses(GetAddressExpensesVO vo) {
        Result<IPage<ChainAddressExpenses>> result = new Result<>();
        LambdaQueryWrapper<ChainAddressExpenses> wrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotEmpty(vo.getSerial())){
            wrapper.eq(ChainAddressExpenses::getSerial,vo.getSerial());
        }
        if(ObjectUtils.isNotNull(vo.getExpensesStatus())){
            wrapper.eq(ChainAddressExpenses::getExpensesStatus,vo.getExpensesStatus());
        }
        IPage<ChainAddressExpenses> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page = addressExpensesMapper.selectPage(page, wrapper);

        result.setResult(page);
        return  result;

    }

    public Result getResultByTxId(String netName,String txId) {
        Result<Object> result = new Result<>();
        JSONObject json = basicService.gettransactioninfo(netName, txId);
        String success = json.getJSONObject("receipt").getString("result");
        if("SUCCESS".equals(success)){
            result.setMessage("成功");
        }else {
            result.error("失败");
        }
        return result;
    }
}
