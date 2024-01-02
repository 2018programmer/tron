package com.dx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pool")
public class ChainPoolAddressController {

    /**
     * 获取地址池的地址明细
     */
    @GetMapping("/list/get")
    public void getPoolAddress(){

    }
}
