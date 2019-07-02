package com.kayak;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kayak.*.mapper")
public class AlgorithmTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgorithmTestApplication.class, args);
    }

}
