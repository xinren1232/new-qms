package com.transcend.plm.alm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/17 10:13
 */
@MapperScan("com.transcend.plm.alm.demandmanagement.common.share")
@SpringBootApplication(scanBasePackages = {"com.transsion","com.transcend"})
public class AlmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlmApplication.class, args);
    }
}