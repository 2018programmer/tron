package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Result;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainNet;
import com.dx.pojo.dto.GetNetByNameDTO;
import com.dx.pojo.dto.NetDTO;
import com.dx.pojo.param.UpdateNetStatusParam;
import com.dx.service.iservice.IChainCoinService;
import com.dx.service.iservice.IChainNetService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetService {

    @Autowired
    private IChainNetService chainNetService;
    @Autowired
    private IChainCoinService chainCoinService;

    /**
     * 修改运行状态
     */
    public Result  updateNetStatus(UpdateNetStatusParam vo){
        Result<Object> result = new Result<>();
        LambdaUpdateWrapper<ChainNet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChainNet::getNetName,vo.getNetName());
        wrapper.set(ChainNet::getRunningStatus,vo.getStatus());
        chainNetService.update(wrapper);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<NetDTO>> getChainNet(Integer runningStatus){
        Result<List<NetDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainNet> netWrapper = Wrappers.lambdaQuery();
        if(runningStatus!=null&&runningStatus==1){
            netWrapper.eq(ChainNet::getRunningStatus,1);
        }
        List<NetDTO> list = new ArrayList<>();
        //获取所有主网名称
        List<ChainNet> chainNets = chainNetService.list(netWrapper);
        if(CollectionUtils.isEmpty(chainNets)){
            result.setResult(list);
            return result;
        }
        for (ChainNet chainNet : chainNets) {
            NetDTO netDTO = new NetDTO();
            BeanUtils.copyProperties(chainNet,netDTO);
            LambdaQueryWrapper<ChainCoin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChainCoin::getNetName,chainNet.getNetName());
            Long num = chainCoinService.count(wrapper);
            netDTO.setCoinNum(num.intValue());
            list.add(netDTO);
        }
        result.setResult(list);

        return result;
        
        
    }

    public Result<GetNetByNameDTO> getNetByName(String netName, String coinName) {
        Result<GetNetByNameDTO> result = new Result<>();
        LambdaQueryWrapper<ChainNet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainNet::getNetName,netName);
        wrapper.eq(ChainNet::getRunningStatus,1);
        ChainNet chainNet = chainNetService.getOne(wrapper);
        GetNetByNameDTO getNetByNameDTO = new GetNetByNameDTO();
        if(ObjectUtils.isEmpty(chainNet)){
            return result.error("对应的主网不存在");
        }
        ChainCoin chainCoin = chainCoinService.getCoinByName(coinName,netName);
        if(ObjectUtils.isEmpty(chainNet)){
            return result.error("对应的主网不存在");
        }
        getNetByNameDTO.setRechargeNetConfirmNum(chainNet.getRechargeNetConfirmNum());
        getNetByNameDTO.setMinNum(chainCoin.getMinNum());
        getNetByNameDTO.setNetName(chainNet.getNetName());
        getNetByNameDTO.setDisplayName(chainNet.getDisplayName());
        getNetByNameDTO.setRunningStatus(chainNet.getRunningStatus());
        result.setResult(getNetByNameDTO);
        return result;
    }

    public Result<List<GetNetByNameDTO>> getNetByCoin(String coinName) {
        Result<List<GetNetByNameDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
        cwrapper.eq(ChainCoin::getCoinName,coinName);
        List<ChainCoin> chainCoins = chainCoinService.list(cwrapper);
        List<GetNetByNameDTO> dtos = new ArrayList<>();
        for (ChainCoin chainCoin : chainCoins) {
            GetNetByNameDTO getNetByNameDTO = new GetNetByNameDTO();
            getNetByNameDTO.setNetName(chainCoin.getNetName());
            getNetByNameDTO.setMinNum(chainCoin.getMinNum());
            LambdaQueryWrapper<ChainNet> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ChainNet::getNetName,chainCoin.getNetName());
            ChainNet chainNet = chainNetService.getOne(wrapper);
            getNetByNameDTO.setRechargeNetConfirmNum(chainNet.getRechargeNetConfirmNum());
            getNetByNameDTO.setDisplayName(chainNet.getDisplayName());
            getNetByNameDTO.setRunningStatus(chainNet.getRunningStatus());
            if(1==chainNet.getRunningStatus()){
                dtos.add(getNetByNameDTO);
            }
        }

        result.setResult(dtos);
        return result;

    }
}
