package com.dx.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dx.common.NetEnum;
import com.dx.dto.HotWalletDTO;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainHotWalletMapper;
import com.dx.service.ChainBasicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BalanceJob {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;

    @Autowired
    private ChainBasicService basicService;

    /**
     * 每分钟查冷热钱包 的余额
     */
    @XxlJob(("queryWalletBalanceTRON"))
    public void queryWalletBalance(){
        //查询钱包所有余额
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName, NetEnum.TRON.getNetName());
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainHotWallets)){
            return;
        }

        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            basicService.queryBalance(NetEnum.TRON.getNetName(), chainHotWallet.getAddress());

        }
    }

    /**
     * 每3分钟查已分配地址的余额
     *
     */
    @XxlJob("queryPoolBalanceTRON")
    public void queryPoolBalance(){

    }
}
