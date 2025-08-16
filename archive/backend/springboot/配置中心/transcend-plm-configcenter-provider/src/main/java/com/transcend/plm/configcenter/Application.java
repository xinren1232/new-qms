package com.transcend.plm.configcenter;

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
@EnableFeignClients(basePackages="com.transcend.plm.feign")
@MapperScan(basePackages = {"com.transcend.plm.configcenter.**.infrastructure.repository.mapper",
        "com.transcend.plm.configcenter.common.dao",
        "com.transcend.plm.configcenter.filemanagement.mapper",
        "com.transcend.plm.configcenter.space.repository.mapper"
})
@EnableAspectJAutoProxy(exposeProxy = true)
@ServletComponentScan(basePackages = "com.transcend")
public class Application{
    public static void main(String[] args) {
    	SpringApplication.run(Application.class,args);
    }
}
