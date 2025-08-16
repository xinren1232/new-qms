package com.transcend.plm.datadriven.config.eventbus.posthandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author bin.yin
 * @date 2024/05/28 11:18
 */
@Component
@ConfigurationProperties(
        prefix = "transcend.datadriven.provider.post-handle"
)
@Getter
public class NotifyPostEventProperties {

    private String codeStrategy;

    private Map<String, String> modelCode2StrategyMap;

    public void setCodeStrategy(String codeStrategy) {
        this.codeStrategy = codeStrategy;
        if (StringUtils.isBlank(codeStrategy)) {
            this.modelCode2StrategyMap = Maps.newHashMap();
        } else {
            this.modelCode2StrategyMap = JSON.parseObject(codeStrategy, new TypeReference<Map<String, String>>() {});
        }
    }
}
