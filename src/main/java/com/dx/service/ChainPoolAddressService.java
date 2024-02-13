package com.dx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.NetEnum;
import com.dx.common.Result;
import com.dx.pojo.dto.*;
import com.dx.entity.*;
import com.dx.mapper.*;
import com.dx.pojo.vo.GetUserAddressVO;
import com.dx.pojo.vo.QueryPoolAddressVO;
import com.dx.pojo.vo.UpdatePoolManageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChainPoolAddressService {


    @Autowired
    private ChainCoinMapper coinMapper;

    @Autowired
    private ChainNetMapper netMapper;

    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

    @Autowired
    private ChainBasicService basicService;

    @Autowired
    private ChainGatherTaskMapper gatherTaskMapper;

    @Autowired
    private ChainAssetsMapper assetsMapper;

    public Result<IPage<CoinManageDTO>> getPoolManage(String netName,Integer pageNum,Integer pageSize) {
        Result<IPage<CoinManageDTO>> result = new Result<>();
        IPage<ChainCoin> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,netName);
        page=coinMapper.selectPage(page,wrapper);
        IPage<CoinManageDTO> convert = page.convert(u -> {
            CoinManageDTO coinManageDTO = new CoinManageDTO();
            BeanUtils.copyProperties(u, coinManageDTO);
            LambdaQueryWrapper<ChainAssets> awrapper = Wrappers.lambdaQuery();
            awrapper.eq(ChainAssets::getNetName,u.getNetName());
            awrapper.eq(ChainAssets::getCoinName,u.getCoinName());
            awrapper.eq(ChainAssets::getAssetType,2);
            List<ChainAssets> chainAssets = assetsMapper.selectList(awrapper);
            BigDecimal reduce = chainAssets.stream()
                    .map(ChainAssets::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            coinManageDTO.setTotalBalance(reduce);
            return coinManageDTO;
        });
        result.setResult(convert);
        return result;
    }

    public Result updatePoolManage(UpdatePoolManageVO vo) {
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,vo.getCoinCode());
        ChainCoin chainCoin = coinMapper.selectOne(wrapper);
        if(Objects.isNull(chainCoin)){
            result.error("币种编码有误");
            return result;
        }
        if(ObjectUtils.isNotNull(vo.getThreshold())){
            chainCoin.setThreshold(vo.getThreshold());
        }
        if(ObjectUtils.isNotNull(vo.getAutoGather())){
            chainCoin.setAutoGather(vo.getAutoGather());
        }


        coinMapper.updateById(chainCoin);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<PoolManageDTO>> getNets() {
        Result<List<PoolManageDTO>> result = new Result<>();
        List<ChainNet> chainNets = netMapper.selectList(null);
        List<PoolManageDTO> poollist = new ArrayList<>();
        for (ChainNet chainNet : chainNets) {
            PoolManageDTO poolManageDTO = new PoolManageDTO();
            poolManageDTO.setNetName(chainNet.getNetName());
            LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ChainPoolAddress::getNetName,chainNet.getNetName());
            Long total = poolAddressMapper.selectCount(wrapper);
            wrapper.eq(ChainPoolAddress::getIsAssigned,0);
            Long noNum = poolAddressMapper.selectCount(wrapper);
            LambdaQueryWrapper<ChainGatherTask> twrapper = Wrappers.lambdaQuery();
            twrapper.eq(ChainGatherTask::getNetName,chainNet.getNetName());
            twrapper.eq(ChainGatherTask::getTaskStatus,1);
            Long status = gatherTaskMapper.selectCount(twrapper);
            poolManageDTO.setTotalNum(total.intValue());
            if(status>=1){
                poolManageDTO.setGatherStatus(1);
            }else {
                poolManageDTO.setGatherStatus(0);
            }
            poolManageDTO.setNoAssignedNum(noNum.intValue());

            poollist.add(poolManageDTO);
        }
        result.setResult(poollist);
        return  result;
    }

    public Result<IPage<PoolAddressDTO>> getPoolAddress(QueryPoolAddressVO vo) {
        Result<IPage<PoolAddressDTO>> result = new Result<>();

        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getNetName,vo.getNetName());
        if(ObjectUtils.isNotNull(vo.getAssignId())){
            wrapper.eq(ChainPoolAddress::getAssignedId,vo.getAssignId());
        }
        if(ObjectUtils.isNotNull(vo.getIsActivated())){
            wrapper.eq(ChainPoolAddress::getIsActivated,vo.getIsActivated());
        }
        if(ObjectUtils.isNotNull(vo.getIsAssigned())){
            wrapper.eq(ChainPoolAddress::getIsAssigned,vo.getIsAssigned());
        }
        if(ObjectUtils.isNotNull(vo.getAssignType())){
            wrapper.eq(ChainPoolAddress::getAssignType,vo.getAssignType());
        }
        IPage<ChainPoolAddress> page = new Page<>(vo.getPageNum(), vo.getPageSize());
        page=poolAddressMapper.selectPage(page,wrapper);

        IPage<PoolAddressDTO> convert = page.convert(u -> {
            PoolAddressDTO poolAddressDTO = new PoolAddressDTO();
            BeanUtils.copyProperties(u, poolAddressDTO);
            poolAddressDTO.setAssignId(u.getAssignedId());
            poolAddressDTO.setEstimateBalance(getContractBalance(u.getAddress()));
            return poolAddressDTO;
        });

        result.setResult(convert);

        return result;
    }

    private BigDecimal getContractBalance(String address) {
        LambdaQueryWrapper<ChainAssets> awrapper = Wrappers.lambdaQuery();
        awrapper.eq(ChainAssets::getAddress,address);
        List<ChainAssets> chainAssets = assetsMapper.selectList(awrapper);
        BigDecimal amount =BigDecimal.ZERO;
        if(CollectionUtils.isEmpty(chainAssets)){
            return amount;
        }
        for (ChainAssets chainAsset : chainAssets) {
            if(Objects.equals(chainAsset.getCoinName(), NetEnum.TRON.getBaseCoin())){
                amount=amount.add(chainAsset.getBalance().multiply(new BigDecimal("0.81")));
            }
            if(Objects.equals(chainAsset.getCoinName(),"USDT")){
                amount=amount.add(chainAsset.getBalance().multiply(new BigDecimal("7.12")));
            }
        }
        return amount.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public Result<GetGatherNumDTO> getGatherNum(String netName) {
        Result<GetGatherNumDTO> result = new Result<>();
        GetGatherNumDTO getGatherNumDTO = new GetGatherNumDTO();
        List<ChainAssets> haveAssets = assetsMapper.getHaveAssets(netName, null,null);
        Set<String> collect = haveAssets.stream().map(ChainAssets::getAddress).collect(Collectors.toSet());
        getGatherNumDTO.setNum(collect.size());
        getGatherNumDTO.setNetName(netName);
        result.setResult(getGatherNumDTO);
        return result;
    }

    public Result  autoCreateAddress(Integer num){
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainNet> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainNet::getRunningStatus,1);
        List<ChainNet> chainNets = netMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(chainNets)){
            return result;
        }
        for (ChainNet chainNet : chainNets) {
            LambdaQueryWrapper<ChainPoolAddress> awrapper = Wrappers.lambdaQuery();
            awrapper.eq(ChainPoolAddress::getNetName,chainNet.getNetName());
            awrapper.eq(ChainPoolAddress::getIsAssigned,0);
            int aNum = poolAddressMapper.selectCount(awrapper).intValue();
            if(aNum>=num){
                continue;
            }
            JSONObject json = basicService.createAddressBynum(chainNet.getNetName(), num-aNum);

            List<ChainPoolAddress> list = JSON.parseArray(json.getString("list"), ChainPoolAddress.class);

            for (ChainPoolAddress chainPoolAddress : list) {
                chainPoolAddress.setCreateTime(System.currentTimeMillis());
                chainPoolAddress.setNetName(chainNet.getNetName());
                chainPoolAddress.setIsActivated(0);
                chainPoolAddress.setIsAssigned(0);
                poolAddressMapper.insert(chainPoolAddress);
            }
        }
        return result;
    }

    public ChainPoolAddress getAddress(String address){
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAddress,address);
        return poolAddressMapper.selectOne(wrapper);

    }

    public Result matchUserAddress(GetUserAddressVO vo) {
        Result<Object> result = new Result<>();
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAssignedId,vo.getAssignedId());
        wrapper.eq(ChainPoolAddress::getAssignType,vo.getAssignType());
        wrapper.eq(ChainPoolAddress::getNetName,vo.getNetName());

        List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(chainPoolAddresses)){
            ChainPoolAddress chainPoolAddress = chainPoolAddresses.get(0);
            result.setResult(chainPoolAddress.getAddress());
            return result;
        }
        wrapper.clear();
        wrapper.eq(ChainPoolAddress::getIsAssigned,0);
        wrapper.orderByAsc(ChainPoolAddress::getId);
        wrapper.last("limit 8");
        List<ChainPoolAddress> address = poolAddressMapper.selectList(wrapper);
        ChainPoolAddress chainPoolAddress = address.get(0);
        chainPoolAddress.setIsAssigned(1);
        chainPoolAddress.setAssignedId(vo.getAssignedId());
        chainPoolAddress.setAssignType(vo.getAssignType());
        poolAddressMapper.updateById(chainPoolAddress);
        result.setResult(chainPoolAddress.getAddress());
        return result;
    }

    public Result<VerifyAddressDTO> verifyAddress(String address,String netName,Integer type) {
        Result<VerifyAddressDTO> result = new Result<>();
        VerifyAddressDTO verifyAddressDTO = new VerifyAddressDTO();
        if(basicService.verifyAddress(address,netName)){
            verifyAddressDTO.setEffective(1);
        }else {
            verifyAddressDTO.setEffective(0);
        }
        if(ObjectUtils.isNotNull(type)&&1==type){
            result.setResult(verifyAddressDTO);
            return result;
        }
        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getAddress,address);
        wrapper.eq(ChainPoolAddress::getNetName,netName);
        wrapper.eq(ChainPoolAddress::getIsAssigned,1);
        ChainPoolAddress chainPoolAddress = poolAddressMapper.selectOne(wrapper);
        if(ObjectUtils.isNotNull(chainPoolAddress)){
            verifyAddressDTO.setIsAssigned(1);
            verifyAddressDTO.setAssignedType(chainPoolAddress.getAssignType());
            verifyAddressDTO.setAssignedId(chainPoolAddress.getAssignedId());
        }else {
            verifyAddressDTO.setIsAssigned(0);
        }

        result.setResult(verifyAddressDTO);
        return result;

    }
}
