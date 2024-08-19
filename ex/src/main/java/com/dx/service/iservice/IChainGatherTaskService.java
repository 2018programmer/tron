package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainGatherTask;

public interface IChainGatherTaskService extends IService<ChainGatherTask> {
    Long getGatheringCount();

    ChainGatherTask getRunningTask(String netName);
}
