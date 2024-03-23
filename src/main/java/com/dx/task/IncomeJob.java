package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.entity.ChainAddressIncome;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainAddressIncomeMapper;
import com.dx.mapper.ChainPoolAddressMapper;
import com.dx.pojo.vo.CreateOrderVO;
import com.dx.service.ApiService;
import com.dx.service.ChainPoolAddressService;
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
    private ChainAddressIncomeMapper incomeMapper;

    @Autowired
    private ApiService apiService;

    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

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
        List<ChainAddressIncome> chainAddressIncomes = incomeMapper.selectList(wrapper);
        log.info("重试调用充值订单:{}",chainAddressIncomes.size());
        if (CollectionUtils.isEmpty(chainAddressIncomes)){
            return;
        }
        for (ChainAddressIncome chainAddressIncome : chainAddressIncomes) {
            log.info("重试调用充值订单:{}",chainAddressIncome.toString());
            CreateOrderVO createOrderVO = new CreateOrderVO();
            LambdaQueryWrapper<ChainPoolAddress> pwrapper = Wrappers.lambdaQuery();
            pwrapper.eq(ChainPoolAddress::getAddress,chainAddressIncome.getAddress());
            ChainPoolAddress poolAddress = poolAddressMapper.selectOne(pwrapper);
            createOrderVO.setExchangeCurrency(chainAddressIncome.getCoinName());
            createOrderVO.setAccountId(poolAddress.getAssignedId());
            createOrderVO.setType(poolAddress.getAssignType());
            createOrderVO.setExchangeAmount(chainAddressIncome.getAmount());
            createOrderVO.setFromAddr(chainAddressIncome.getFromAddress());
            createOrderVO.setToAddr(chainAddressIncome.getAddress());
            createOrderVO.setTranId(chainAddressIncome.getTxId());
            createOrderVO.setMainNet(1);
            log.info("充值订单请求参数:{}",createOrderVO);
            String orderId = null;
            try{
                JSONObject jsonObject = apiService.createOrder(createOrderVO);
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
            incomeMapper.updateById(chainAddressIncome);
        }

    }
}
