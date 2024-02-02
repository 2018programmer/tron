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
        //矿工费钱包
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
        //热钱包
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainHotWallet::getNetName, NetEnum.TRON.getNetName());
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);

        if(!CollectionUtils.isEmpty(chainHotWallets)) {
            for (ChainHotWallet chainHotWallet : chainHotWallets) {
                BigDecimal amount = basicService.queryBalance(NetEnum.TRON.getNetName(), chainHotWallet.getAddress());
                // 添加或更新资产记录
                LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                aswrapper.eq(ChainAssets::getAddress, chainHotWallet.getAddress());
                aswrapper.eq(ChainAssets::getCoinName, NetEnum.TRON.getBaseCoin());
                ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                if (ObjectUtils.isNull(chainAssets)) {
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        chainAssets = new ChainAssets();
                        chainAssets.setAddress(chainHotWallet.getAddress());
                        chainAssets.setBalance(amount);
                        chainAssets.setAssetType(1);
                        chainAssets.setNetName(NetEnum.TRON.getNetName());
                        chainAssets.setCoinCode("trx");
                        chainAssets.setCoinName(NetEnum.TRON.getBaseCoin());
                        assetsMapper.insert(chainAssets);
                    }

                } else {
                    chainAssets.setBalance(amount);
                    assetsMapper.updateById(chainAssets);
                }
            }
        }
        //地址池
        LambdaQueryWrapper<ChainPoolAddress> aWrapper = Wrappers.lambdaQuery();
        aWrapper.eq(ChainPoolAddress::getNetName,NetEnum.TRON.getNetName());
        aWrapper.eq(ChainPoolAddress::getIsAssigned,1);
        List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(aWrapper);
        if(!CollectionUtils.isEmpty(chainPoolAddresses)){
            for (ChainPoolAddress poolAddress : chainPoolAddresses) {
                BigDecimal amount = basicService.queryBalance(NetEnum.TRON.getNetName(), poolAddress.getAddress());
                // 添加或更新资产记录
                LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                aswrapper.eq(ChainAssets::getAddress,poolAddress.getAddress());
                aswrapper.eq(ChainAssets::getCoinName,NetEnum.TRON.getBaseCoin());
                ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                if(ObjectUtils.isNull(chainAssets)){
                    if(amount.compareTo(BigDecimal.ZERO)>0){
                        chainAssets =new ChainAssets();
                        chainAssets.setAddress(poolAddress.getAddress());
                        chainAssets.setBalance(amount);
                        chainAssets.setNetName(NetEnum.TRON.getNetName());
                        chainAssets.setAssetType(2);
                        chainAssets.setCoinCode("trx");
                        chainAssets.setCoinName(NetEnum.TRON.getBaseCoin());
                        assetsMapper.insert(chainAssets);
                    }

                }else {
                    chainAssets.setBalance(amount);
                    assetsMapper.updateById(chainAssets);
                }
            }
        }
        long end = System.currentTimeMillis();
        log.info("查冷热钱包地址池链主币的余额结束--------------------------耗时{}秒",(end-start)/1000);

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
        for (ChainCoin chainCoin : chainCoins) {
            //热钱包
            LambdaQueryWrapper<ChainHotWallet> hwrapper = new LambdaQueryWrapper<>();
            hwrapper.eq(ChainHotWallet::getNetName, NetEnum.TRON.getNetName());
            List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(hwrapper);
            if (!CollectionUtils.isEmpty(chainHotWallets)){
                for (ChainHotWallet hot : chainHotWallets) {
                    BigDecimal amount = basicService.queryContractBalance(NetEnum.TRON.getNetName(), chainCoin.getCoinCode(), hot.getAddress());
                    // 添加或更新资产记录
                    LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                    aswrapper.eq(ChainAssets::getAddress,hot.getAddress());
                    aswrapper.eq(ChainAssets::getCoinName,chainCoin.getCoinName());
                    ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                    if(ObjectUtils.isNull(chainAssets)){
                        if(amount.compareTo(BigDecimal.ZERO)>0) {
                            chainAssets = new ChainAssets();
                            chainAssets.setAddress(hot.getAddress());
                            chainAssets.setBalance(amount);
                            chainAssets.setAssetType(1);
                            chainAssets.setNetName(NetEnum.TRON.getNetName());
                            chainAssets.setCoinCode(chainCoin.getCoinCode());
                            chainAssets.setCoinName(chainCoin.getCoinName());
                            assetsMapper.insert(chainAssets);
                        }
                    }else {
                        chainAssets.setBalance(amount);
                        assetsMapper.updateById(chainAssets);
                    }
                }
            }

            //地址池
            LambdaQueryWrapper<ChainPoolAddress> aWrapper = Wrappers.lambdaQuery();
            aWrapper.eq(ChainPoolAddress::getNetName,NetEnum.TRON.getNetName());
            aWrapper.eq(ChainPoolAddress::getIsAssigned,1);
            List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(aWrapper);
            if (!CollectionUtils.isEmpty(chainPoolAddresses)){
                for (ChainPoolAddress pool : chainPoolAddresses) {
                    BigDecimal amount = basicService.queryContractBalance(NetEnum.TRON.getNetName(), chainCoin.getCoinCode(), pool.getAddress());
                    // 添加或更新资产记录
                    LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                    aswrapper.eq(ChainAssets::getAddress,pool.getAddress());
                    aswrapper.eq(ChainAssets::getCoinName,chainCoin.getCoinName());
                    ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                    if(ObjectUtils.isNull(chainAssets)){
                        if(amount.compareTo(BigDecimal.ZERO)>0) {
                            chainAssets = new ChainAssets();
                            chainAssets.setAddress(pool.getAddress());
                            chainAssets.setBalance(amount);
                            chainAssets.setAssetType(2);
                            chainAssets.setNetName(NetEnum.TRON.getNetName());
                            chainAssets.setCoinCode(chainCoin.getCoinCode());
                            chainAssets.setCoinName(chainCoin.getCoinName());
                            assetsMapper.insert(chainAssets);
                        }
                    }else {
                        chainAssets.setBalance(amount);
                        assetsMapper.updateById(chainAssets);
                    }
                }
            }

        }
        long end = System.currentTimeMillis();
        log.info("查冷热钱包地址池链合约币的余额结束--------------------------耗时{}秒",(end-start)/1000);
    }



}
