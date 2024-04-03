package com.dx.service;

import com.dx.common.Result;
import com.dx.pojo.dto.GetStatisticsDTO;
import com.dx.service.iservice.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    @Autowired
    private IChainNetService chainNetService;

    @Autowired
    private IChainCoinService chainCoinService;

    @Autowired
    private IChainPoolAddressService chainPoolAddressService;

    @Autowired
    private IChainHotWalletService chainHotWalletService;

    @Autowired
    private IChainGatherTaskService chainGatherTaskService;

    @Autowired
    private IChainAddressExpensesService chainAddressExpensesService;

    public Result<GetStatisticsDTO> getStatistics() {
        Result<GetStatisticsDTO> result = new Result<>();
        GetStatisticsDTO getStatisticsDTO = new GetStatisticsDTO();
        getStatisticsDTO.setNetCount(chainNetService.getOnNetCount().intValue());
        getStatisticsDTO.setCoinCount(chainCoinService.getCoinCount().intValue());
        getStatisticsDTO.setActiveAddressCount(chainPoolAddressService.getActiveAddressCount().intValue());
        getStatisticsDTO.setHotWalletCount(chainHotWalletService.getOnHotWalletCount().intValue());
        getStatisticsDTO.setGatherCount(chainGatherTaskService.getGatheringCount().intValue());
        getStatisticsDTO.setExpensesQueueCount(0);
        getStatisticsDTO.setExpensesingCount(0);
        getStatisticsDTO.setExpensesWrongCount(chainAddressExpensesService.getExpensesWrongCount().intValue());
        return result;
    }
}
