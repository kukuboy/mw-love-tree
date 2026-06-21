package com.lovetree;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lovetree.mapper")
public class LoveTreeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoveTreeApplication.class, args);
    }

}
