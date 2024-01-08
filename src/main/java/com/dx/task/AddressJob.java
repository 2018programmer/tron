package com.dx.task;


import com.dx.service.ChainPoolAddressService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class AddressJob {

    @Autowired
    private ChainPoolAddressService poolAddressService;


    @Value("${base.address-num}")
    private Integer num;

    @XxlJob(("createAddressTaskTRON"))
    public void createAddressTaskTRON(){
        poolAddressService.autoCreateAddress(num);
    }
}
