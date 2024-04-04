package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainGatherTask;
import com.dx.mapper.ChainGatherTaskMapper;
import com.dx.service.iservice.IChainGatherTaskService;
import org.springframework.stereotype.Service;

@Service
public class ChainGatherTaskServiceImpl extends ServiceImpl<ChainGatherTaskMapper, ChainGatherTask> implements IChainGatherTaskService {
    @Override
    public Long getGatheringCount() {
        LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherTask::getTaskStatus,1);
        return this.count(wrapper);
    }

    @Override
    public ChainGatherTask getRunningTask(String netName) {
        LambdaQueryWrapper<ChainGatherTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherTask::getNetName,netName);
        wrapper.eq(ChainGatherTask::getTaskStatus,1);
        return getOne(wrapper);
    }
}
