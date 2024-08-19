package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainHotWallet;

import java.util.List;


public interface IChainHotWalletService extends IService<ChainHotWallet> {
    Long getOnHotWalletCount();

    List<ChainHotWallet> getOnHotWalletList(String netName);

    List<ChainHotWallet> getHotWalletsByNet(String netName);
}
