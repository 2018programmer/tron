package com.dx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dx.entity.ChainAssets;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChainAssetsMapper extends BaseMapper<ChainAssets> {

    /**
     * 获取资产大于阀值的资产
     * @param netName
     * @return
     */
    List<ChainAssets> getHaveAssets(String netName, String coinName);
}
