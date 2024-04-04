package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainColdWallet;

public interface IChainColdWalletService extends IService<ChainColdWallet> {
    ChainColdWallet getByNet(String netName);
}
