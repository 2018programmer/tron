package com.dx.task;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dx.common.Constant;
import com.dx.common.RedisUtil;
import com.dx.entity.ChainAddressIncome;
import com.dx.entity.ChainCoin;
import com.dx.entity.ChainFlow;
import com.dx.entity.ChainPoolAddress;
import com.dx.mapper.ChainAddressIncomeMapper;
import com.dx.mapper.ChainCoinMapper;
import com.dx.mapper.ChainFlowMapper;
import com.dx.mapper.ChainPoolAddressMapper;
import com.dx.pojo.dto.ContactDTO;
import com.dx.pojo.dto.GetCurrencyListDTO;
import com.dx.pojo.vo.CreateOrderVO;
import com.dx.service.ApiService;
import com.dx.service.BasicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonitorJob {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BasicService basicService;
    @Autowired
    private ChainPoolAddressMapper poolAddressMapper;

    @Autowired
    private ChainAddressIncomeMapper incomeMapper;
    @Autowired
    private ChainCoinMapper coinMapper;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ApiService apiService;

    @Autowired
    private ChainFlowMapper flowMapper;
    @XxlJob("monitorTransferTRON")
    public void monitorTransferTRON()  {
        var numRedis =0;
        Boolean has = redisUtil.hasKey(Constant.RedisKey.HITCOUNTER);
        var numOnline = 0;
        // 查询区块计数表 获取当前区块 没有则设值
        var tronNum = basicService.getnowblock("TRON");
        if(ObjectUtils.isNotNull(tronNum)){
            //延迟5块 方便监听区分哪些是归集打的钱
            numOnline =tronNum-5;
        }
        if(0==numOnline){
            return;
        }
        if(!has){
            numRedis=numOnline;
            redisUtil.setCacheObject(Constant.RedisKey.HITCOUNTER, numOnline);
        }else {
            numRedis = redisUtil.getCacheObject(Constant.RedisKey.HITCOUNTER) ;
            log.info("当前redis记录块高为{}",numRedis);
        }
        if (numRedis>numOnline){
            return;
        }
        for (int i = numRedis; i <= numOnline; i++) {
            //获取区块信息

            String tron = basicService.getblockbynum("TRON", i);
            if(ObjectUtils.isNull(tron)){
                redisUtil.increment(Constant.RedisKey.HITCOUNTER, 1);
                continue;
            }
            List<ContactDTO> list = JSONUtil.toList(tron, ContactDTO.class);
            LambdaQueryWrapper<ChainPoolAddress> wrapper = Wrappers.lambdaQuery();
            List<ChainPoolAddress> chainPoolAddresses = poolAddressMapper.selectList(wrapper);
            DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
//            defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);
            try {

                for (ContactDTO contactDTO : list) {
                    LambdaQueryWrapper<ChainFlow> fwrapper = Wrappers.lambdaQuery();
                    fwrapper.eq(ChainFlow::getTxId,contactDTO.getTxId());
                    List<ChainFlow> chainFlows = flowMapper.selectList(fwrapper);
                    if(CollectionUtils.isNotEmpty(chainFlows)){
                        continue;
                    }
                    //匹配信息 更新表数据 更新区块
                    List<ChainPoolAddress> collect = chainPoolAddresses.stream().filter(o -> (o.getAddress().equals(contactDTO.getToAddress()))).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(collect)){
                        continue;
                    }
                    log.info("正在处理的充值信息{}",contactDTO);
                    ChainPoolAddress chainPoolAddress = collect.get(0);
                    //添加收款监听记录
                    ChainAddressIncome chainAddressIncome = new ChainAddressIncome();
                    chainAddressIncome.setFromAddress(contactDTO.getFromAddress());
                    chainAddressIncome.setAddress(chainPoolAddress.getAddress());
                    chainAddressIncome.setCreateTime(System.currentTimeMillis());
                    LambdaQueryWrapper<ChainCoin> coinwrapper = Wrappers.lambdaQuery();
                    coinwrapper.eq(ChainCoin::getCoinCode,contactDTO.getCoinCode());
                    ChainCoin chainCoin = coinMapper.selectOne(coinwrapper);
                    chainAddressIncome.setNetName(chainCoin.getNetName());
                    chainAddressIncome.setTxId(contactDTO.getTxId());
                    List<GetCurrencyListDTO> currencyList = apiService.getCurrencyList();
                    long count = currencyList.stream().filter(o -> o.getCurrency().equals(chainCoin.getCoinName())).count();
                    chainAddressIncome.setChainConfirm(1);
                    chainAddressIncome.setCoinName(chainCoin.getCoinName());
                    chainAddressIncome.setAmount(contactDTO.getAmount());

                    //记录流水
                    ChainFlow chainFlow = new ChainFlow();
                    chainFlow.setGroupId(String.valueOf(System.currentTimeMillis()));
                    chainFlow.setAddress(contactDTO.getToAddress());
                    chainFlow.setFlowWay(1);
                    chainFlow.setAmount(contactDTO.getAmount());
                    chainFlow.setTransferType(1);
                    chainFlow.setCoinName(chainCoin.getCoinName());
                    chainFlow.setTxId(contactDTO.getTxId());
                    chainFlow.setWalletType(1);
                    chainFlow.setTargetAddress(contactDTO.getFromAddress());
                    chainFlow.setNetName(chainCoin.getNetName());
                    chainFlow.setCreateTime(System.currentTimeMillis());
                    JSONObject reason = new JSONObject();
                    reason.put("success",true);

                    if (Objects.isNull(chainPoolAddress.getAssignedId())){
                        reason.put("success",false);
                        reason.put("reason","地址未分配");
                    }
                    if (chainCoin.getMinNum().compareTo(contactDTO.getAmount())>0){
                        reason.put("success",false);
                        reason.put("reason","不满足最小收款数");
                    }

                    if(count<=0){
                        reason.put("success",false);
                        reason.put("reason","未配置相关交易对");
                    }
                    if (chainPoolAddress.getIsDelete()==1){
                        reason.put("success",false);
                        reason.put("reason","地址已删除");
                    }
                    if(reason.getBoolean("success")){
                        chainAddressIncome.setEffective(1);
                        //创建充值订单
                        CreateOrderVO createOrderVO = new CreateOrderVO();
                        createOrderVO.setExchangeCurrency(chainCoin.getCoinName());
                        createOrderVO.setAccountId(chainPoolAddress.getAssignedId());
                        createOrderVO.setType(chainPoolAddress.getAssignType());
                        createOrderVO.setExchangeAmount(contactDTO.getAmount());
                        createOrderVO.setFromAddr(contactDTO.getFromAddress());
                        createOrderVO.setToAddr(contactDTO.getToAddress());
                        createOrderVO.setTranId(contactDTO.getTxId());
                        createOrderVO.setMainNet(1);
                        log.info("充值订单请求参数:{}",createOrderVO);
                        //新建进程调用创建订单
                        String orderId=null;
                        try{
                            JSONObject jsonObject = apiService.createOrder(createOrderVO);
                            Boolean success = jsonObject.getBoolean("success");
                            if (!Objects.isNull(success) && true == success) {
                                orderId=jsonObject.getJSONObject("result").getString("orderId");
                                reason.put("log",jsonObject);
                            }else {
                                reason.put("reason","订单服务返回false");
                            }
                        }catch (Exception e){
                            log.info("链充值调用订单服务失败{}",e.getMessage());
                            reason.put("reason","订单服务调用报错");
                            reason.put("log",e.getMessage());
                            reason.put("logstack",e.getStackTrace());
                        }
                        chainAddressIncome.setSerial(orderId);
                    }else {
                        chainAddressIncome.setEffective(0);
                    }
                    chainAddressIncome.setOrderLog(reason.toJSONString());
                    flowMapper.insert(chainFlow);
                    incomeMapper.insert(chainAddressIncome);
                }
                // 提交事务
                redisUtil.increment(Constant.RedisKey.HITCOUNTER, 1);
                transactionManager.commit(status);
            } catch (Exception e) {
                log.info("监听发生逻辑异常{}",e.getMessage());
                log.info("监听发生逻辑异常栈信息{}",e.getStackTrace());
            }
        }
    }

}
