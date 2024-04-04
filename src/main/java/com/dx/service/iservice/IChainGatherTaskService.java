package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainGatherTask;

import java.util.List;

public interface IChainGatherTaskService extends IService<ChainGatherTask> {
    Long getGatheringCount();

    List<ChainGatherTask> getRunningTask(String netName);
}
