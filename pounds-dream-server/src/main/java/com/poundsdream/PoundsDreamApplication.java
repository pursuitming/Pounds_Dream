package com.poundsdream;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.poundsdream.mapper")
@EnableScheduling
public class PoundsDreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoundsDreamApplication.class, args);
    }
}
