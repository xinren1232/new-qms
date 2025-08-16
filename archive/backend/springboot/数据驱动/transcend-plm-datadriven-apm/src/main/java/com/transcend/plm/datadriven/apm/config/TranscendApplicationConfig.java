package com.transcend.plm.datadriven.apm.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author unknown
 */
@EnableFeignClients(basePackages="com.transcend.plm.**")
@MapperScan(basePackages = {"com.transcend.plm.datadriven.**.infrastructure.**.repository.mapper","com.transcend.plm.datadriven.notify.mapper","com.transcend.plm.datadriven.common.dao",
        "com.transcend.plm.datadriven.apm.**.mapper", "com.transcend.plm.datadriven.filemanager.mapper"})
@EnableAspectJAutoProxy(exposeProxy = true)
@ServletComponentScan(basePackages = "com.transcend")
@Configuration
public class TranscendApplicationConfig {
}
