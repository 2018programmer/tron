package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Result;
import com.dx.pojo.dto.GetNetByNameDTO;
import com.dx.pojo.dto.NetDTO;
import com.dx.pojo.vo.UpdateNetStatusVO;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainNet;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainNetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetService {
    
    @Autowired
    private ChainNetMapper netMapper;
    
    @Autowired
    private ChainCoinMapper coinMapper;

    /**
     * 修改运行状态
     */
    public Result  updateNetStatus(UpdateNetStatusVO vo){
        Result<Object> result = new Result<>();
        LambdaUpdateWrapper<ChainNet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChainNet::getNetName,vo.getNetName());
        wrapper.set(ChainNet::getRunningStatus,vo.getStatus());
        netMapper.update(wrapper);
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
        List<ChainNet> chainNets = netMapper.selectList(netWrapper);
        if(CollectionUtils.isEmpty(chainNets)){
            result.setResult(list);
            return result;
        }
        for (ChainNet chainNet : chainNets) {
            NetDTO netDTO = new NetDTO();
            BeanUtils.copyProperties(chainNet,netDTO);
            LambdaQueryWrapper<ChainCoin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChainCoin::getNetName,chainNet.getNetName());
            Long num = coinMapper.selectCount(wrapper);
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
        ChainNet chainNet = netMapper.selectOne(wrapper);
        GetNetByNameDTO getNetByNameDTO = new GetNetByNameDTO();
        if(ObjectUtils.isEmpty(chainNet)){
            return result.error("对应的主网不存在");
        }
        LambdaQueryWrapper<ChainCoin> cwrapper = Wrappers.lambdaQuery();
        cwrapper.eq(ChainCoin::getCoinName,coinName);
        cwrapper.eq(ChainCoin::getNetName,netName);
        ChainCoin chainCoin = coinMapper.selectOne(cwrapper);
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
        List<ChainCoin> chainCoins = coinMapper.selectList(cwrapper);
        List<GetNetByNameDTO> dtos = new ArrayList<>();
        for (ChainCoin chainCoin : chainCoins) {
            GetNetByNameDTO getNetByNameDTO = new GetNetByNameDTO();
            getNetByNameDTO.setNetName(chainCoin.getNetName());
            getNetByNameDTO.setMinNum(chainCoin.getMinNum());
            LambdaQueryWrapper<ChainNet> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ChainNet::getNetName,chainCoin.getNetName());
            ChainNet chainNet = netMapper.selectOne(wrapper);
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
