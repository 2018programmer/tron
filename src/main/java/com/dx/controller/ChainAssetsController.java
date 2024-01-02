package com.dx.controller;

import com.dx.service.ChainAssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets")
public class ChainAssetsController {

    @Autowired
    private ChainAssetsService chainAssetsService;
    @GetMapping("/hot/list/get")
    public void getHotwalletBalance(){

    }
}
