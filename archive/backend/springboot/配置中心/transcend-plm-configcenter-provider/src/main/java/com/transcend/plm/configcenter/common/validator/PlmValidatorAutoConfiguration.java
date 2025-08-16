package com.transcend.plm.configcenter.common.validator;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
@Configuration
@AutoConfigureBefore(ValidationAutoConfiguration.class)
public class PlmValidatorAutoConfiguration {

    /**
     * 自定义的spring参数校验器，重写主要为了保存一些在自定义validator中读不到的属性
     *
     */
    @Bean
    public PlmValidatorFactoryBean plmValidator() {
        return new PlmValidatorFactoryBean();
    }

}
