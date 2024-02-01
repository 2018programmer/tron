package com.dx.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.service.ChainBasicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BalanceJob {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    @Autowired
    private ChainFeeWalletMapper feeWalletMapper;

    @Autowired
    private ChainBasicService basicService;
    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

    @Autowired
    private ChainAssetsMapper assetsMapper;
    @Autowired
    private ChainCoinMapper coinMapper;

    /**
     * 查冷热钱包 地址池 链主币的余额
     */
    @XxlJob(("querybaseBalanceTRON"))
    public void querybaseBalanceTRON(){
        log.info("查冷热钱包地址池链主币的余额--------------------------");
        long start = System.currentTimeMillis();
        LambdaQueryWrapper<ChainFeeWallet> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.eq(ChainFeeWallet::getNetName,NetEnum.TRON.getNetName());
        List<ChainFeeWallet> chainFeeWallets = feeWalletMapper.selectList(feeWrapper);
        if(!CollectionUtils.isEmpty(chainFeeWallets)){
            for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
                chainFeeWallet.setBalance(basicService.queryBalance(NetEnum.TRON.getNetName(), chainFeeWallet.getAddress()));
                feeWalletMapper.updateById(chainFeeWallet);
            }
        }

        List<String> addressList = getAddressList();

        if(!CollectionUtils.isEmpty(addressList)){
            for (String address : addressList) {
                BigDecimal amount = basicService.queryBalance(NetEnum.TRON.getNetName(), address);
                // 添加或更新资产记录
                LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                aswrapper.eq(ChainAssets::getAddress,address);
                aswrapper.eq(ChainAssets::getCoinName,NetEnum.TRON.getBaseCoin());
                ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                if(ObjectUtils.isNull(chainAssets)&& amount.compareTo(BigDecimal.ZERO)>0){
                    chainAssets =new ChainAssets();
                    chainAssets.setAddress(address);
                    chainAssets.setBalance(amount);
                    chainAssets.setNetName(NetEnum.TRON.getNetName());
                    chainAssets.setCoinCode("trx");
                    chainAssets.setCoinName(NetEnum.TRON.getBaseCoin());
                    assetsMapper.insert(chainAssets);
                }else {
                    chainAssets.setBalance(amount);
                    assetsMapper.updateById(chainAssets);
                }
            }
        }
        long end = System.currentTimeMillis();
        log.info("查冷热钱包地址池链主币的余额结束--------------------------耗时{}",(end-start)/1000);

    }

    /**
     * 查询热钱包 地址池合约币余额
     *
     */
    @XxlJob("queryContractBalanceTRON")
    public void queryContractBalanceTRON(){
        log.info("查冷热钱包地址池链合约主币的余额--------------------------");
        long start = System.currentTimeMillis();

        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,NetEnum.TRON.getNetName());
        wrapper.eq(ChainCoin::getCoinType,"contract");
        List<ChainCoin> chainCoins = coinMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainCoins)){
            return;
        }
        List<String> addressList = getAddressList();
        if (CollectionUtils.isEmpty(addressList)){
            return;
        }
        for (ChainCoin chainCoin : chainCoins) {
            for (String address : addressList) {
                BigDecimal amount = basicService.queryContractBalance(NetEnum.TRON.getNetName(), chainCoin.getCoinCode(), address);
                // 添加或更新资产记录
                LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                aswrapper.eq(ChainAssets::getAddress,address);
                aswrapper.eq(ChainAssets::getCoinName,chainCoin.getCoinName());
                ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                if(ObjectUtils.isNull(chainAssets)&& amount.compareTo(BigDecimal.ZERO)>0){
                    chainAssets =new ChainAssets();
                    chainAssets.setAddress(address);
                    chainAssets.setBalance(amount);
                    chainAssets.setNetName(NetEnum.TRON.getNetName());
                    chainAssets.setCoinCode(chainCoin.getCoinCode());
                    chainAssets.setCoinName(chainCoin.getCoinName());
                    assetsMapper.insert(chainAssets);
                }else {
                    chainAssets.setBalance(amount);
                    assetsMapper.updateById(chainAssets);
                }
            }
        }
        long end = System.currentTimeMillis();
        log.info("查冷热钱包地址池链合约币的余额结束--------------------------耗时{}",(end-start)/1000);
    }

    private List<String> getAddressList(){
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName, NetEnum.TRON.getNetName());
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        List<String> hotAddressList = chainHotWallets.stream().map(ChainHotWallet::getAddress).collect(Collectors.toList());

        LambdaQueryWrapper<ChainPoolAddress> aWrapper = Wrappers.lambdaQuery();
        aWrapper.eq(ChainPoolAddress::getNetName,NetEnum.TRON.getNetName());
        aWrapper.eq(ChainPoolAddress::getIsAssigned,1);
        List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(aWrapper);
        List<String> poolAddressList = chainPoolAddresses.stream().map(ChainPoolAddress::getAddress).collect(Collectors.toList());

        hotAddressList.addAll(poolAddressList);

        return hotAddressList;
    }


}
