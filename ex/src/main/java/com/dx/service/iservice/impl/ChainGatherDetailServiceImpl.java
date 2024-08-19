package com.dx.service.iservice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dx.entity.ChainGatherDetail;
import com.dx.mapper.ChainGatherDetailMapper;
import com.dx.service.iservice.IChainGatherDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainGatherDetailServiceImpl extends ServiceImpl<ChainGatherDetailMapper, ChainGatherDetail> implements IChainGatherDetailService {
    @Override
    public Long getFinishCount(Integer id) {
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getTaskId,id).eq(ChainGatherDetail::getGatherStatus,3);
        return count(wrapper);
    }

    @Override
    public List<ChainGatherDetail> getTaskDetails(Integer id) {
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getTaskId,id);
        return list(wrapper);
    }

    @Override
    public IPage<ChainGatherDetail> getTaskDetailsPage(IPage<ChainGatherDetail> page,Integer id) {
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getTaskId,id);
        return page(page,wrapper);
    }

    @Override
    public void cancelGatherDetail(Integer id) {
        //归集子任务取消
        LambdaUpdateWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ChainGatherDetail::getTaskId,id);
        wrapper.and((w)->{
            w.eq(ChainGatherDetail::getGatherStatus,0).or().eq(ChainGatherDetail::getGatherStatus,2);
        });
        wrapper.set(ChainGatherDetail::getGatherStatus,4);
        update(wrapper);
    }

    @Override
    public List<ChainGatherDetail> getNotStartDetail(Integer id) {
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getGatherStatus,0);
        wrapper.eq(ChainGatherDetail::getTaskId,id);
        wrapper.orderByDesc(ChainGatherDetail::getId);
        return list(wrapper);
    }

    @Override
    public List<ChainGatherDetail> getGoingDetail(Integer id) {
        LambdaQueryWrapper<ChainGatherDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainGatherDetail::getGatherStatus,2);
        wrapper.eq(ChainGatherDetail::getTaskId,id);
        wrapper.le(ChainGatherDetail::getTryTime,5);
        wrapper.orderByAsc(ChainGatherDetail::getTryTime);
        return list(wrapper);
    }
}
