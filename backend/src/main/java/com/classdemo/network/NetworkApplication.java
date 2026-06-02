package com.classdemo.network;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.classdemo.network.mapper")
public class NetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkApplication.class, args);
    }
}
