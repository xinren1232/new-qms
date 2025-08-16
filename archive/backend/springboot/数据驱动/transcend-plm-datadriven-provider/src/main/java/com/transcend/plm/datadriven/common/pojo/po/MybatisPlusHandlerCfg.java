package com.transcend.plm.datadriven.common.pojo.po;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.config.MybatisPlusExtendsProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @Program transcend-plm-datadriven
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

    /**
     * 动态表名插件
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(new TranscendTableNameHandler(Arrays.asList("notify_config","notify_config_operate","notify_config_trigger","notify_trigger_rule","notify_time_rule")));
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }
}
