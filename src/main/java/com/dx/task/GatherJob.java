package com.dx.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.entity.ChainGatherDetail;
import com.dx.entity.ChainGatherTask;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainGatherDetailMapper;
import com.dx.mapper.ChainGatherTaskMapper;
import com.dx.service.ChainBasicService;
import com.dx.service.ChainGatherService;
import com.dx.service.ChainPoolAddressService;
import com.dx.service.other.ChainOperateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GatherJob {


    @Autowired
    private ChainGatherDetailMapper gatherDetailMapper;

    @Autowired
    private ChainOperateService operateService;

    @Autowired
    private ChainGatherTaskMapper gatherTaskMapper;

    @Autowired
    private ChainPoolAddressService poolAddressService;

    @Autowired
    private ChainBasicService basicService;

    public void executeGatherJob(){
        LambdaQueryWrapper<ChainGatherTask> twrapper = Wrappers.lambdaQuery();
        twrapper.eq(ChainGatherTask::getTaskStatus,1);
        twrapper.eq(ChainGatherTask::getNetName,"TRON");
        ChainGatherTask chainGatherTask = gatherTaskMapper.selectOne(twrapper);
        if(ObjectUtils.isNull(chainGatherTask)){
            return;
        }
        //扫描任务明细
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getGatherStatus,0);
        wrapper.eq(ChainGatherDetail::getTaskId,chainGatherTask.getId());
        List<ChainGatherDetail> chainGatherDetails = gatherDetailMapper.selectList(wrapper);

        if(CollectionUtils.isEmpty(chainGatherDetails)){
            chainGatherTask.setTaskStatus(5);
            chainGatherTask.setEndTime(System.currentTimeMillis());
            gatherTaskMapper.updateById(chainGatherTask);
            return;
        }
        ChainGatherDetail chainGatherDetail = chainGatherDetails.get(0);

        ChainPoolAddress address = poolAddressService.getAddress(chainGatherDetail.getGatherAddress());

        String txId = operateService.addressToGather(chainGatherTask.getAddress(), chainGatherDetail.getGatherAddress(), address.getPrivateKey(), chainGatherDetail.getCoinCode(), chainGatherDetail.getAmount());


        JSONObject tron = basicService.gettransactioninfo("TRON", txId);

        //解析记录

    }
}
