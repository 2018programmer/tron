package com.dx.service.iservice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dx.entity.ChainGatherDetail;

import java.util.List;

public interface IChainGatherDetailService extends IService<ChainGatherDetail> {
    Long getFinishCount(Integer id);

    List<ChainGatherDetail> getTaskDetails(Integer id);

    IPage<ChainGatherDetail> getTaskDetailsPage(IPage<ChainGatherDetail> page,Integer id);

    void cancelGatherDetail(Integer id);
}
