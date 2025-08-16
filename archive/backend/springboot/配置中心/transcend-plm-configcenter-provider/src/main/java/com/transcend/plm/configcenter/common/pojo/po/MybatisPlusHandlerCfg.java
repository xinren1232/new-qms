package com.transcend.plm.configcenter.common.pojo.po;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.config.MybatisPlusExtendsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-23 15:26
 **/
@Configuration
public class MybatisPlusHandlerCfg {

    private final MybatisPlusExtendsProperties properties;

    public MybatisPlusHandlerCfg(MybatisPlusExtendsProperties properties) {
        this.properties = properties;
    }
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new TanscendMybatisPlusMetaHandler(this.properties.getMetaProps());
    }
}
