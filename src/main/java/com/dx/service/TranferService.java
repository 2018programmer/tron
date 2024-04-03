package com.dx.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.entity.ChainAddressExpenses;
import com.dx.mapper.ChainAddressExpensesMapper;
import com.dx.pojo.vo.ConfirmOrderVO;
import com.dx.pojo.vo.GetAddressExpensesVO;
import com.dx.pojo.vo.GetAddressIncomeVO;
import com.dx.entity.ChainAddressIncome;
import com.dx.mapper.ChainAddressIncomeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TranferService {

    @Autowired
    private ChainAddressIncomeMapper addressIncomeMapper;

    @Autowired
    private ChainAddressExpensesMapper addressExpensesMapper;
    @Autowired
    private BasicService basicService;


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
        wrapper.orderByDesc(ChainAddressIncome::getId);
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
        wrapper.orderByDesc(ChainAddressExpenses::getId);
        IPage<ChainAddressExpenses> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page = addressExpensesMapper.selectPage(page, wrapper);

        result.setResult(page);
        return  result;

    }

    public Result getResultByTxId(String netName,String txId) {
        Result<Object> result = new Result<>();
        JSONObject json = basicService.gettransactioninfo(netName, txId);
        JSONObject receipt = json.getJSONObject("receipt");

        if(receipt.containsKey("result")){
            if("SUCCESS".equals(receipt.getString("result"))){
                result.setMessage("成功");
            }else {
                result.error("失败");
            }
        }else {
            if(receipt.containsKey("net_usage")||receipt.containsKey("net_fee")){
                result.setMessage("成功");
            }else {
                result.error("失败");
            }
        }

        return result;
    }

    public Result confirmOrder(ConfirmOrderVO vo) {
        log.info("确认订单参数为{}",vo);

        Result<Object> result = new Result<>();
        LambdaUpdateWrapper<ChainAddressIncome> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ChainAddressIncome::getTxId, vo.getTxId());
        wrapper.set(ChainAddressIncome::getSerial, vo.getSerial());
        addressIncomeMapper.update(wrapper);
        return result;
    }
}
