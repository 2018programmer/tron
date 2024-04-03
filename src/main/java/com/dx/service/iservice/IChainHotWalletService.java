package com.dx.service.iservice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainHotWallet;
import org.springframework.stereotype.Service;

@Service
public interface IChainHotWalletService extends IService<ChainHotWallet> {
    Long getOnHotWalletCount();
}
