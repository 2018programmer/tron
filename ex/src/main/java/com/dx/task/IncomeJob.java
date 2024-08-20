package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.entity.ChainAddressIncome;
import com.dx.entity.ChainPoolAddress;
import com.dx.pojo.param.CreateOrderParam;
import com.dx.service.ApiService;
import com.dx.service.iservice.IChainAddressIncomeService;
import com.dx.service.iservice.IChainPoolAddressService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class IncomeJob {
    @Autowired
    private IChainAddressIncomeService chainAddressIncomeService;

    @Autowired
    private ApiService apiService;
    @Autowired
    private IChainPoolAddressService chainPoolAddressService;

    /**
     * 重试调用充值订单
     */
    @XxlJob("retryIncomeOrder")
    public void retryIncomeOrder(){
        LambdaQueryWrapper<ChainAddressIncome> wrapper = Wrappers.lambdaQuery();
        wrapper.isNull(ChainAddressIncome::getSerial);
        wrapper.isNotNull(ChainAddressIncome::getFromAddress);
        wrapper.orderByDesc(ChainAddressIncome::getId);
        wrapper.gt(ChainAddressIncome::getCreateTime,System.currentTimeMillis()-24*60*60*1000);
        wrapper.eq(ChainAddressIncome::getEffective,1);
        wrapper.last("limit 10");
        List<ChainAddressIncome> chainAddressIncomes = chainAddressIncomeService.list(wrapper);
        log.info("重试调用充值订单:{}",chainAddressIncomes.size());
        if (CollectionUtils.isEmpty(chainAddressIncomes)){
            return;
        }
        for (ChainAddressIncome chainAddressIncome : chainAddressIncomes) {
            log.info("重试调用充值订单:{}",chainAddressIncome.toString());
            CreateOrderParam createOrderParam = new CreateOrderParam();
            ChainPoolAddress poolAddress = chainPoolAddressService.getByAddress(chainAddressIncome.getAddress());
            createOrderParam.setExchangeCurrency(chainAddressIncome.getCoinName());
            createOrderParam.setAccountId(poolAddress.getAssignedId());
            createOrderParam.setType(poolAddress.getAssignType());
            createOrderParam.setExchangeAmount(chainAddressIncome.getAmount());
            createOrderParam.setFromAddr(chainAddressIncome.getFromAddress());
            createOrderParam.setToAddr(chainAddressIncome.getAddress());
            createOrderParam.setTranId(chainAddressIncome.getTxId());
            createOrderParam.setMainNet(1);
            log.info("充值订单请求参数:{}", createOrderParam);
            String orderId = null;
            try{
                JSONObject jsonObject = apiService.createOrder(createOrderParam);
                chainAddressIncome.setOrderLog(jsonObject.toJSONString());
                Boolean success = jsonObject.getBoolean("success");
                if (!Objects.isNull(success) && true == success) {
                    orderId=jsonObject.getJSONObject("result").getString("orderId");
                }
            }catch (Exception e){
                e.printStackTrace();
                if(ObjectUtils.isEmpty(chainAddressIncome.getOrderLog())){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("result","订单服务错误,请排查,并且等待重试");
                    chainAddressIncome.setOrderLog(jsonObject.toJSONString());
                }
            }
            chainAddressIncome.setSerial(orderId);
            chainAddressIncomeService.updateById(chainAddressIncome);
        }

    }
}