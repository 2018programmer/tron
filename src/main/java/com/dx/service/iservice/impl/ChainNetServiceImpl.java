package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainNet;
import com.dx.mapper.ChainNetMapper;
import com.dx.service.iservice.IChainNetService;
import org.springframework.stereotype.Service;

@Service
public class ChainNetServiceImpl extends ServiceImpl<ChainNetMapper, ChainNet> implements IChainNetService {
    @Override
    public Long getOnNetCount() {
        LambdaQueryWrapper<ChainNet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainNet::getRunningStatus,1);
        return this.count(wrapper);

    }
}
