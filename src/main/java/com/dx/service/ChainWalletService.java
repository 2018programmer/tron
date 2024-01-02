package com.dx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dx.common.Result;
import com.dx.dto.AddWalletDTO;
import com.dx.dto.HotWalletDTO;
import com.dx.dto.UpdateColdWalletDTO;
import com.dx.dto.UpdateHotWalletStatusDTO;
import com.dx.entity.ChainColdWallet;
import com.dx.entity.ChainHotWallet;
import com.dx.mapper.ChainColdWalletMapper;
import com.dx.mapper.ChainHotWalletMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChainWalletService {

    @Autowired
    private ChainHotWalletMapper hotWalletMapper;
    
    @Autowired
    private ChainColdWalletMapper coldWalletMapper;

    public Result updateColdWallet(UpdateColdWalletDTO dto){
        Result<Object> result = new Result<>();
        ChainColdWallet chainColdWallet = coldWalletMapper.selectById(dto.getId());
        if(Objects.isNull(chainColdWallet)) {
            result.error("参数有误,该对象不存在");
            return result;
        }
        chainColdWallet.setAddress(dto.getAddress());
        coldWalletMapper.updateById(chainColdWallet);
        result.setMessage("操作成功");
        return result;
    }

    public void  addFeeWallet(){
        //增加矿工费钱包 并且查询 矿工费钱包的余额
    }

    public Result<List<HotWalletDTO>> getHotWallet(String netName) {
        Result<List<HotWalletDTO>> result = new Result<>();
        LambdaQueryWrapper<ChainHotWallet> wrapper = new LambdaQueryWrapper<>();
        if(Strings.isNotEmpty(netName)){
            wrapper.eq(ChainHotWallet::getNetName,netName);
        }
        List<ChainHotWallet> chainHotWallets = hotWalletMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainHotWallets)){
            result.error("没有数据");
            return result;
        }
        List<HotWalletDTO> list = new ArrayList<>();
        for (ChainHotWallet chainHotWallet : chainHotWallets) {
            HotWalletDTO hotWalletDTO = new HotWalletDTO();
            BeanUtils.copyProperties(chainHotWallet,hotWalletDTO);
            hotWalletDTO.setInCount(0);
            hotWalletDTO.setOutCount(0);
            hotWalletDTO.setBalance(BigDecimal.ZERO);
            hotWalletDTO.setConvertBalance(BigDecimal.ZERO);
            list.add(hotWalletDTO);
        }
        result.setResult(list);
        return result;
    }

    public Result updateHotWalletStatus(UpdateHotWalletStatusDTO dto) {
        Result<Object> result = new Result<>();
        ChainHotWallet chainHotWallet = hotWalletMapper.selectById(dto.getId());
        if(Objects.isNull(chainHotWallet)){
            result.error("没有数据");
            return result;
        }
        chainHotWallet.setRunningStatus(dto.getRunningStatus());
        hotWalletMapper.updateById(chainHotWallet);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<ChainColdWallet>> getColdWallets() {
        Result<List<ChainColdWallet>> result = new Result<>();
        List<ChainColdWallet> chainColdWallets = coldWalletMapper.selectList(null);
        if(CollectionUtils.isEmpty(chainColdWallets)){
            result.error("没有数据");
            return result;
        }
        result.setResult(chainColdWallets);
        return result;
    }

    public Result addHotWallet(AddWalletDTO dto) {
        Result<Object> result = new Result<>();
        //生成地址
        String netName = dto.getNetName();

        result.setMessage("操作成功");
        return result;
    }

    public Result addfeeWallet(AddWalletDTO dto) {
        Result<Object> result = new Result<>();

        result.setMessage("操作成功");
        return result;
    }
}
