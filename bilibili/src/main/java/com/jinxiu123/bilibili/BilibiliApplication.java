package com.jinxiu123.bilibili;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jinxiu123.bilibili.mapper")
@EnableScheduling
public class BilibiliApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilibiliApplication.class, args);
    }

}
