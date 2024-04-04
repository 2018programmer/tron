package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainFlow;

public interface IChainFlowService extends IService<ChainFlow> {
    Long getWalletOutCount(String address);

    Long getWalletInCount(String address);
}
