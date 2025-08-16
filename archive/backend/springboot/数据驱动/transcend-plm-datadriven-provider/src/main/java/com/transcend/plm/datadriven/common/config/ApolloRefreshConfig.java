package com.transcend.plm.datadriven.common.config;

import com.transsion.framework.tool.config.ConfigChange;
import com.transsion.framework.tool.config.ConfigChangeCustomizer;
import com.transsion.framework.tool.config.ConfigChangeItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Set;

/**
 * apollo刷新配置
 *
 * @author yinbin
 * @date 2023/10/31 10:45
 */
@Slf4j
@Configuration
public class ApolloRefreshConfig {
    @Resource
    private ApplicationContext applicationContext;

    /**
     * @return {@link ConfigChangeCustomizer }
     */
    @Bean
    public ConfigChangeCustomizer commontConfigChangeCustomizer() {
        return this::updateConfig;
    }

    /**
     * @param configChange
     */
    private void updateConfig(ConfigChange configChange) {
        Set<String> changedKeys = configChange.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChangeItem changeByKey = configChange.getChangeByKey(changedKey);
            log.info(String.format("Apollo Found change - key: 【%s】, oldValue: 【%s】, newValue: 【%s】, changeType: 【%s】",
                    changeByKey.getPropertyName(), changeByKey.getOldValue(), changeByKey.getNewValue(), changeByKey.getChangeType()));
        }
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changedKeys));
    }
}
