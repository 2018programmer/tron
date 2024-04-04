package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainFeeWallet;
import com.dx.mapper.ChainFeeWalletMapper;
import com.dx.service.iservice.IChainFeeWalletService;
import org.springframework.stereotype.Service;

@Service
public class ChainFeeWalletServiceImpl extends ServiceImpl<ChainFeeWalletMapper, ChainFeeWallet> implements IChainFeeWalletService {
}
