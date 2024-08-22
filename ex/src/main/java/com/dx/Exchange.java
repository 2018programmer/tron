package com.dx;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class Exchange {

    public static void main(String[] args) {
        SpringApplication.run(Exchange.class, args);
    }
}
