package com.dx.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.dto.ContactDTO;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.service.ChainBasicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonitorJob {
    
    @Autowired
    private ChainBasicService chainBasicService;

    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

    @Autowired
    private ChainAddressIncomeMapper incomeMapper;

    @Autowired
    private ChainAssetsMapper assetsMapper;

    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private HitCounterMapper hitCounterMapper ;
    @XxlJob("monitorTransferTRON")
    public void monitorTransferTRON()  {
        var numsql =0;
        HitCounter hit = hitCounterMapper.selectById(1);
        numsql = hit.getCnt();
        var numOnline = 0;
        // 查询区块计数表 获取当前区块 没有则设值
        Integer tronNum = chainBasicService.getnowblock("TRON");
        if(ObjectUtils.isNotNull(tronNum)){
            numOnline =tronNum;
        }

        if(0==numOnline){
            return;
        }
        if(numsql==0){
            numsql=numOnline;
            hit.setCnt(numsql);
            hitCounterMapper.updateById(hit);
        }
        if (numsql>numOnline){
            return;
        }
        for (int i = numsql; i <= numOnline; i++) {
            //获取区块信息
            String tron = chainBasicService.getblockbynum("TRON", i);
            if(ObjectUtils.isNull(tron)){
                try{
                    Thread.sleep(500);
                }catch (Exception e){

                }
                continue;
            }
            List<ContactDTO> list = JSONUtil.toList(tron, ContactDTO.class);
            if(CollectionUtils.isEmpty(list)){
                continue;
            }
            LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
            List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(wrapper);
            for (ContactDTO contactDTO : list) {
                //匹配信息 更新表数据 更新区块
                List<ChainPoolAddress> collect = chainPoolAddresses.stream().filter(o -> o.getAddress().equals(contactDTO.getToAddress())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(collect)){
                    continue;
                }
                ChainPoolAddress chainPoolAddress = collect.get(0);
                //添加收款监听记录
                ChainAddressIncome chainAddressIncome = new ChainAddressIncome();
                chainAddressIncome.setAddress(chainPoolAddress.getAddress());
                chainAddressIncome.setCreateTime(System.currentTimeMillis());
                LambdaQueryWrapper<ChainCoin> coinwrapper = Wrappers.lambdaQuery();
                coinwrapper.eq(ChainCoin::getCoinCode,contactDTO.getCoinCode());
                ChainCoin chainCoin = coinMapper.selectOne(coinwrapper);
                chainAddressIncome.setNetName(chainCoin.getNetName());
                chainAddressIncome.setTxId(contactDTO.getTxId());
                chainAddressIncome.setEffective(0);
                chainAddressIncome.setChainConfirm(1);
                incomeMapper.insert(chainAddressIncome);
                // 添加或更新资产记录
                LambdaQueryWrapper<ChainAssets> aswrapper = Wrappers.lambdaQuery();
                aswrapper.eq(ChainAssets::getAddress,chainPoolAddress.getAddress());
                aswrapper.eq(ChainAssets::getCoinCode,contactDTO.getCoinCode());
                ChainAssets chainAssets = assetsMapper.selectOne(aswrapper);
                if(ObjectUtils.isNull(chainAssets)){
                    chainAssets =new ChainAssets();
                    chainAssets.setAddress(chainPoolAddress.getAddress());
                    chainAssets.setBalance(contactDTO.getAmount());
                    chainAssets.setNetName(chainCoin.getNetName());
                    chainAssets.setCoinCode(chainCoin.getCoinCode());
                    chainAssets.setCoinName(chainCoin.getCoinName());
                    assetsMapper.insert(chainAssets);
                }else {
                    BigDecimal balance = chainAssets.getBalance();
                    balance.add(contactDTO.getAmount());
                    assetsMapper.updateById(chainAssets);
                }
            }
        }
        hit.setCnt(numOnline);
        hitCounterMapper.updateById(hit);
    }

}
