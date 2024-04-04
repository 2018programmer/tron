package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainNet;

import java.util.List;

public interface IChainNetService extends IService<ChainNet> {

    Long getOnNetCount();

    List<ChainNet> getRunningNets();
}
