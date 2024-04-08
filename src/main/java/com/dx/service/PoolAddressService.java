package com.dx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dx.common.Result;
import com.dx.entity.*;
import com.dx.pojo.dto.*;
import com.dx.pojo.param.*;
import com.dx.service.iservice.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PoolAddressService {

    @Autowired
    private IChainCoinService chainCoinService;
    @Autowired
    private IChainNetService chainNetService;
    @Autowired
    private IChainPoolAddressService chainPoolAddressService;
    @Autowired
    private BasicService basicService;
    @Autowired
    private IChainGatherTaskService chainGatherTaskService;
    @Autowired
    private IChainAssetsService chainAssetsService;
    @Autowired
    private ApiService apiService;

    @Autowired
    private IChainThirdOrderService chainThirdOrderService;
    public Result<IPage<CoinManageDTO>> getPoolManage(String netName,Integer pageNum,Integer pageSize) {
        Result<IPage<CoinManageDTO>> result = new Result<>();
        IPage<ChainCoin> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getNetName,netName);
        page=chainCoinService.page(page,wrapper);
        IPage<CoinManageDTO> convert = page.convert(u -> {
            CoinManageDTO coinManageDTO = new CoinManageDTO();
            BeanUtils.copyProperties(u, coinManageDTO);
            List<ChainAssets> chainAssets = chainAssetsService.getAssetsBytype(u.getNetName(),u.getCoinName(),2);
            BigDecimal reduce = chainAssets.stream()
                    .map(ChainAssets::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            coinManageDTO.setTotalBalance(reduce);
            return coinManageDTO;
        });
        result.setResult(convert);
        return result;
    }

    public Result updatePoolManage(UpdatePoolManageParam vo) {
        Result<Object> result = new Result<>();
        ChainCoin chainCoin = chainCoinService.getCoinByCode(vo.getCoinCode());
        LambdaQueryWrapper<ChainCoin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainCoin::getCoinCode,vo.getCoinCode());
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
        chainCoinService.updateById(chainCoin);
        result.setMessage("操作成功");
        return result;
    }

    public Result<List<PoolManageDTO>> getNets() {
        Result<List<PoolManageDTO>> result = new Result<>();
        List<ChainNet> chainNets = chainNetService.list();
        List<PoolManageDTO> poollist = new ArrayList<>();
        for (ChainNet chainNet : chainNets) {
            PoolManageDTO poolManageDTO = new PoolManageDTO();
            poolManageDTO.setNetName(chainNet.getNetName());
            Long total = chainPoolAddressService.getCount(chainNet.getNetName());
            Long noNum = chainPoolAddressService.getNoAssignedNum(chainNet.getNetName());
            ChainGatherTask runningTask = chainGatherTaskService.getRunningTask(chainNet.getNetName());
            poolManageDTO.setTotalNum(total.intValue());
            if(ObjectUtils.isNotNull(runningTask)){
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

    public Result<IPage<PoolAddressDTO>> getPoolAddress(QueryPoolAddressParam vo) {
        Result<IPage<PoolAddressDTO>> result = new Result<>();

        LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChainPoolAddress::getIsDelete,0);
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
        page=chainPoolAddressService.page(page,wrapper);
        Map<String, String> priceList = apiService.getPriceList();
        IPage<PoolAddressDTO> convert = page.convert(u -> {
            PoolAddressDTO poolAddressDTO = new PoolAddressDTO();
            BeanUtils.copyProperties(u, poolAddressDTO);
            poolAddressDTO.setAssignId(u.getAssignedId());
            poolAddressDTO.setEstimateBalance(getContractBalance(u.getAddress(),priceList));
            return poolAddressDTO;
        });

        result.setResult(convert);

        return result;
    }

    private BigDecimal getContractBalance(String address,Map<String,String> map) {
        if(CollectionUtils.isEmpty(map)){
            return BigDecimal.ZERO;
        }

        List<ChainAssets> chainAssets = chainAssetsService.getAddressAssets(address);
        BigDecimal amount =BigDecimal.ZERO;
        if(CollectionUtils.isEmpty(chainAssets)){
            return amount;
        }
        for (ChainAssets chainAsset : chainAssets) {
            BigDecimal num = BigDecimal.ZERO;
            if (!StringUtils.isEmpty(map.get(chainAsset.getCoinName()))){
                num =new BigDecimal(map.get(chainAsset.getCoinName()));
            }
            amount=amount.add(chainAsset.getBalance().multiply(num));
        }
        return amount.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public Result<GetGatherNumDTO> getGatherNum(String netName) {
        Result<GetGatherNumDTO> result = new Result<>();
        GetGatherNumDTO getGatherNumDTO = new GetGatherNumDTO();
        List<ChainAssets> haveAssets = chainAssetsService.getHaveAssets(netName, null,null);
        Set<String> collect = haveAssets.stream().map(ChainAssets::getAddress).collect(Collectors.toSet());
        getGatherNumDTO.setNum(collect.size());
        getGatherNumDTO.setNetName(netName);
        result.setResult(getGatherNumDTO);
        return result;
    }

    public Result  autoCreateAddress(Integer num){
        Result<Object> result = new Result<>();
        List<ChainNet> chainNets = chainNetService.getRunningNets();
        if(CollectionUtils.isEmpty(chainNets)){
            return result;
        }
        for (ChainNet chainNet : chainNets) {

            int aNum = chainPoolAddressService.getNoAssignedNum(chainNet.getNetName()).intValue();
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
                chainPoolAddressService.save(chainPoolAddress);
            }
        }
        return result;
    }


    public Result matchUserAddress(GetUserAddressParam vo) {
        Result<Object> result = new Result<>();

        List<ChainPoolAddress> chainPoolAddresses = chainPoolAddressService.getByAssigned(vo.getAssignedId(),vo.getAssignType(),vo.getNetName());
        if(!CollectionUtils.isEmpty(chainPoolAddresses)){
            ChainPoolAddress chainPoolAddress = chainPoolAddresses.get(0);
            result.setResult(chainPoolAddress.getAddress());
            return result;
        }

        List<ChainPoolAddress> address = chainPoolAddressService.getMatchAddress();
        ChainPoolAddress chainPoolAddress = address.get(0);
        chainPoolAddress.setIsAssigned(1);
        chainPoolAddress.setAssignedId(vo.getAssignedId());
        chainPoolAddress.setAssignType(vo.getAssignType());
        chainPoolAddressService.updateById(chainPoolAddress);
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

        ChainPoolAddress chainPoolAddress = chainPoolAddressService.getValidAddress(address,netName);
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

    public Result unbindAddress(UnbindAddressParam vo) {
        chainPoolAddressService.unbindAddress(vo.getAddress());
        return Result.ok();
    }

    public Result bindThirdOrder(BindThirdOrderParam param) {
        Result<Object> result = new Result<>();


        List<ChainThirdOrder> list =chainThirdOrderService.getAvailableAddress(param.getNetName());
        if (CollectionUtils.isEmpty(list)){
            return result.error("暂时没有可用地址,请添加");
        }
        ChainThirdOrder chainThirdOrder = list.get(0);
        long curr = System.currentTimeMillis();
        chainThirdOrder.setSerial(param.getMerchantId()+":"+param.getThirdSerial());
        chainThirdOrder.setUnbindTime(curr+30*60*1000+30*1000);
        chainThirdOrderService.updateById(chainThirdOrder);
        //取消之前的绑定
        chainThirdOrderService.cancelSameBind(param.getMerchantId(),param.getThirdSerial(),chainThirdOrder.getAddress());
        return result;
    }

    public Result addThirdOrderAddress(AddThirdOrderAddressParam param){
        Result<Object> result = new Result<>();
        JSONObject json = basicService.createAddressBynum(param.getNetName(), param.getNum());

        List<ChainPoolAddress> list = JSON.parseArray(json.getString("list"), ChainPoolAddress.class);
        for (ChainPoolAddress poolAddress : list) {
            poolAddress.setCreateTime(System.currentTimeMillis());
            poolAddress.setNetName(param.getNetName());
            poolAddress.setIsActivated(0);
            poolAddress.setIsAssigned(1);
            poolAddress.setAssignType(4);
            chainPoolAddressService.save(poolAddress);
            ChainThirdOrder chainThirdOrder = new ChainThirdOrder();
            chainThirdOrder.setAddress(poolAddress.getAddress());
            chainThirdOrder.setNetName(param.getNetName());
            chainThirdOrderService.save(chainThirdOrder);
        }
        return result;
    }
}
