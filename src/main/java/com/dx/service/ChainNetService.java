package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dx.common.Result;
import com.dx.dto.NetDTO;
import com.dx.dto.UpdateNetStatusDTO;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainNet;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainMainNetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChainNetService {
    
    @Autowired
    private ChainMainNetMapper netMapper;
    
    @Autowired
    private ChainCoinMapper coinMapper;

    /**
     * 修改运行状态
     */
    public Result  updateNetStatus(UpdateNetStatusDTO dto){
        Result<Object> result = new Result<>();
        LambdaUpdateWrapper<ChainNet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChainNet::getNetName,dto.getNetName());
        wrapper.set(ChainNet::getRunningStatus,dto.getStatus());
        netMapper.update(wrapper);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<NetDTO>> getChainNet(){
        Result<List<NetDTO>> result = new Result<>();

        //获取所有主网名称
        List<ChainNet> chainNets = netMapper.selectList(null);
        if(CollectionUtils.isEmpty(chainNets)){
            result.error("没有数据");
            return result;
        }

        List<NetDTO> list = new ArrayList<>();
        for (ChainNet chainNet : chainNets) {
            NetDTO netDTO = new NetDTO();
            BeanUtils.copyProperties(chainNet,netDTO);
            netDTO.setLogo("没有logo");
            LambdaQueryWrapper<ChainCoin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChainCoin::getNetName,chainNet.getNetName());
            Long num = coinMapper.selectCount(wrapper);
            netDTO.setCoinNum(num.intValue());
            list.add(netDTO);
        }
        result.setResult(list);

        return result;
        
        
    }
}
