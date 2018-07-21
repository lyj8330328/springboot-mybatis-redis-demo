package com.example.springbootmybatisredisdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class SpringbootMybatisRedisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMybatisRedisDemoApplication.class, args);
    }
}
