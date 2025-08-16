package com.transcend.plm.pi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/17 10:13
 */
@SpringBootApplication(scanBasePackages = {"com.transsion","com.transcend"})
public class PiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PiApplication.class, args);
    }
}