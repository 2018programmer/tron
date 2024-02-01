package com.dx.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dx.common.NetEnum;
import com.dx.entity.ChainFeeWallet;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.mapper.ChainHotWalletMapper;
import com.dx.service.ChainBasicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class BalanceJob {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;

    @Autowired
    private ChainBasicService basicService;

    /**
     * 每分钟查冷热钱包 链主币的余额
     */
    @XxlJob(("queryWalletBalanceTRON"))
    public void queryWalletBalance(){
        //查询钱包所有余额
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName, NetEnum.TRON.getNetName());
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(chainHotWallets)){
            for (ChainHotWallet chainHotWallet : chainHotWallets) {
                chainHotWallet.setBalance(basicService.queryBalance(NetEnum.TRON.getNetName(), chainHotWallet.getAddress()));
                hotWalletMapper.updateById(chainHotWallet);
            }
        }

        LambdaQueryWrapper<ChainFeeWallet> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.eq(ChainFeeWallet::getNetName,NetEnum.TRON.getNetName());
        List<ChainFeeWallet> chainFeeWallets = feeWalletMapper.selectList(feeWrapper);
        if(!CollectionUtils.isEmpty(chainFeeWallets)){
            for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
                chainFeeWallet.setBalance(basicService.queryBalance(NetEnum.TRON.getNetName(), chainFeeWallet.getAddress()));
                feeWalletMapper.updateById(chainFeeWallet);
            }
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
