package com.dx.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BalanceJob {

    /**
     * 每分钟查冷热钱包 的余额
     */
    @XxlJob(("queryWalletBalanceTRON"))
    public void queryWalletBalance(){
        //查询钱包所有余额
    }

    /**
     * 每3分钟查已分配地址的余额
     *
     */
    @XxlJob("queryPoolBalanceTRON")
    public void queryPoolBalance(){

    }
}
