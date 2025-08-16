package com.transcend.plm.datadriven;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 *
 * @author TRANSCEND
 *
 */
@SpringBootApplication(scanBasePackages = {"com.transsion","com.transcend"})
@EnableFeignClients(basePackages="com.transcend.plm.**")
@MapperScan(basePackages = {"com.transcend.plm.datadriven.**.infrastructure.**.repository.mapper","com.transcend.plm.datadriven.common.dao","com.transcend.plm.datadriven.domain.draft"})
@EnableAspectJAutoProxy(exposeProxy = true)
@ServletComponentScan(basePackages = "com.transcend")
public class Application{
    public static void main(String[] args) {
    	SpringApplication.run(Application.class,args);
    }
}
