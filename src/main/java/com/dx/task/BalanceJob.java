package com.dx.task;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.dx.common.NetEnum;
import com.dx.entity.*;
import com.dx.service.BasicService;
import com.dx.service.iservice.*;
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
    private IChainHotWalletService chainHotWalletService;
    @Autowired
    private IChainFeeWalletService chainFeeWalletService;
    @Autowired
    private BasicService basicService;
    @Autowired
    private IChainPoolAddressService chainPoolAddressService;
    @Autowired
    private IChainAssetsService chainAssetsService;
    @Autowired
    private IChainCoinService chainCoinService;

    /**
     * 查冷热钱包 地址池 链主币的余额
     */
    @XxlJob("querybaseBalanceTRON")
    public void querybaseBalanceTRON(){
        log.info("查冷热钱包地址池链主币的余额--------------------------");
        //矿工费钱包
        long start = System.currentTimeMillis();

        List<ChainFeeWallet> chainFeeWallets = chainFeeWalletService.getByNet(NetEnum.TRON.getNetName());
        if(!CollectionUtils.isEmpty(chainFeeWallets)){
            for (ChainFeeWallet chainFeeWallet : chainFeeWallets) {
                chainFeeWallet.setBalance(basicService.queryBalance(NetEnum.TRON.getNetName(), chainFeeWallet.getAddress()));
                chainFeeWalletService.updateById(chainFeeWallet);
                try {

                    Thread.sleep(200);
                }catch (Exception e){

                }
            }
        }
        //热钱包

        List<ChainHotWallet> chainHotWallets =chainHotWalletService.getHotWalletsByNet(NetEnum.TRON.getNetName());
        if(!CollectionUtils.isEmpty(chainHotWallets)) {
            for (ChainHotWallet chainHotWallet : chainHotWallets) {
                BigDecimal amount = basicService.queryBalance(NetEnum.TRON.getNetName(), chainHotWallet.getAddress());
                ChainAssets chainAssets = chainAssetsService.getAssetOne(chainHotWallet.getAddress(), NetEnum.TRON.getBaseCoin());
                if (ObjectUtils.isNull(chainAssets)) {
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        chainAssets = new ChainAssets();
                        chainAssets.setAddress(chainHotWallet.getAddress());
                        chainAssets.setBalance(amount);
                        chainAssets.setAssetType(1);
                        chainAssets.setNetName(NetEnum.TRON.getNetName());
                        chainAssets.setCoinCode("trx");
                        chainAssets.setCoinName(NetEnum.TRON.getBaseCoin());
                        chainAssetsService.save(chainAssets);
                    }

                } else {
                    chainAssets.setBalance(amount);
                    chainAssetsService.updateById(chainAssets);
                }
                try {

                    Thread.sleep(200);
                }catch (Exception e){

                }
            }
        }
        //地址池

        List<ChainPoolAddress> chainPoolAddresses = chainPoolAddressService.getAssignedByNet(NetEnum.TRON.getNetName());
        if(!CollectionUtils.isEmpty(chainPoolAddresses)){
            for (ChainPoolAddress poolAddress : chainPoolAddresses) {
                BigDecimal amount = basicService.queryBalance(NetEnum.TRON.getNetName(), poolAddress.getAddress());
                // 添加或更新资产记录
                ChainAssets chainAssets = chainAssetsService.getAssetOne(poolAddress.getAddress(),NetEnum.TRON.getBaseCoin());
                if(ObjectUtils.isNull(chainAssets)){
                    if(amount.compareTo(BigDecimal.ZERO)>0){
                        chainAssets =new ChainAssets();
                        chainAssets.setAddress(poolAddress.getAddress());
                        chainAssets.setBalance(amount);
                        chainAssets.setNetName(NetEnum.TRON.getNetName());
                        chainAssets.setAssetType(2);
                        chainAssets.setCoinCode("trx");
                        chainAssets.setCoinName(NetEnum.TRON.getBaseCoin());
                        chainAssetsService.save(chainAssets);
                    }

                }else {
                    chainAssets.setBalance(amount);
                    chainAssetsService.updateById(chainAssets);
                }
            }
            try {

                Thread.sleep(200);
            }catch (Exception e){

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

        List<ChainCoin> chainCoins = chainCoinService.getContractCoin(NetEnum.TRON.getNetName());
        if(CollectionUtils.isEmpty(chainCoins)){
            return;
        }
        for (ChainCoin chainCoin : chainCoins) {
            //热钱包
            List<ChainHotWallet> chainHotWallets = chainHotWalletService.getHotWalletsByNet(NetEnum.TRON.getNetName());
            if (!CollectionUtils.isEmpty(chainHotWallets)){
                for (ChainHotWallet hot : chainHotWallets) {
                    BigDecimal amount = basicService.queryContractBalance(NetEnum.TRON.getNetName(), chainCoin.getCoinCode(), hot.getAddress());
                    // 添加或更新资产记录
                    ChainAssets chainAssets = chainAssetsService.getAssetOne(hot.getAddress(),chainCoin.getCoinName());
                    if(ObjectUtils.isNull(chainAssets)){
                        if(amount.compareTo(BigDecimal.ZERO)>0) {
                            chainAssets = new ChainAssets();
                            chainAssets.setAddress(hot.getAddress());
                            chainAssets.setBalance(amount);
                            chainAssets.setAssetType(1);
                            chainAssets.setNetName(NetEnum.TRON.getNetName());
                            chainAssets.setCoinCode(chainCoin.getCoinCode());
                            chainAssets.setCoinName(chainCoin.getCoinName());
                            chainAssetsService.save(chainAssets);
                        }
                    }else {
                        chainAssets.setBalance(amount);
                        chainAssetsService.updateById(chainAssets);
                    }
                }
                try {

                    Thread.sleep(200);
                }catch (Exception e){

                }
            }

            //地址池
            List<ChainPoolAddress> chainPoolAddresses = chainPoolAddressService.getAssignedByNet(NetEnum.TRON.getNetName());
            if (!CollectionUtils.isEmpty(chainPoolAddresses)){
                for (ChainPoolAddress pool : chainPoolAddresses) {
                    BigDecimal amount = basicService.queryContractBalance(NetEnum.TRON.getNetName(), chainCoin.getCoinCode(), pool.getAddress());
                    // 添加或更新资产记录
                    ChainAssets chainAssets = chainAssetsService.getAssetOne(pool.getAddress(),chainCoin.getCoinName());
                    if(ObjectUtils.isNull(chainAssets)){
                        if(amount.compareTo(BigDecimal.ZERO)>0) {
                            chainAssets = new ChainAssets();
                            chainAssets.setAddress(pool.getAddress());
                            chainAssets.setBalance(amount);
                            chainAssets.setAssetType(2);
                            chainAssets.setNetName(NetEnum.TRON.getNetName());
                            chainAssets.setCoinCode(chainCoin.getCoinCode());
                            chainAssets.setCoinName(chainCoin.getCoinName());
                            chainAssetsService.save(chainAssets);
                        }
                    }else {
                        chainAssets.setBalance(amount);
                        chainAssetsService.updateById(chainAssets);
                    }
                }
                try {

                    Thread.sleep(200);
                }catch (Exception e){

                }
            }

        }
        long end = System.currentTimeMillis();
        log.info("查冷热钱包地址池链合约币的余额结束--------------------------耗时{}秒",(end-start)/1000);
    }



}
