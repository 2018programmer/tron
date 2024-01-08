package com.dx.task;


import com.dx.service.ChainPoolAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class AddressJob {

    @Autowired
    private ChainPoolAddressService poolAddressService;


    private static final  Integer num =300;

    public void createAddressTask(){
        poolAddressService.autoCreateAddress(num);
    }
}
