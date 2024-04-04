package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainFlow;
import com.dx.mapper.ChainFlowMapper;
import com.dx.service.iservice.IChainFlowService;
import org.springframework.stereotype.Service;

@Service
public class ChainFlowServiceImpl extends ServiceImpl<ChainFlowMapper, ChainFlow> implements IChainFlowService {
}
