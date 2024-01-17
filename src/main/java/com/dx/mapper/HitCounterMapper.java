package com.dx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dx.entity.HitCounter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HitCounterMapper extends BaseMapper<HitCounter> {
}
