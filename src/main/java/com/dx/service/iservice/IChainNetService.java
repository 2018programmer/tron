package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainNet;

public interface IChainNetService extends IService<ChainNet> {

    Long getOnNetCount();
}
