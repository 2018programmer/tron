package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainFeeWallet;

import java.util.List;

public interface IChainFeeWalletService extends IService<ChainFeeWallet> {
    List<ChainFeeWallet> getByNet(String netName);
}
