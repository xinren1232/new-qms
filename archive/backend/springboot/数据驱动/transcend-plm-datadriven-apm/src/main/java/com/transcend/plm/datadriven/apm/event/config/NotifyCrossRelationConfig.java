package com.transcend.plm.datadriven.apm.event.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 跨级新增关系配置
 * @author yinbin
 * @version:
 * @date 2023/10/25 20:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "transcend.plm.apm.cross.relation")
public class NotifyCrossRelationConfig {

    private String config;

    private Map<String, NotifyCross> notifyCrossConfigMap;

    private String revisionRelTaskConfig;

    private Map<String, NotifyRevisionRelTaskConfig> notifyRevisionRelTaskConfigMap;

    public void setConfig(String config) {
        this.config = config;
        if (StringUtils.isNotBlank(config)) {
            this.notifyCrossConfigMap = JSON.parseObject(config, new TypeReference<Map<String, NotifyCross>>() {});
        }
    }

    public void setRevisionRelTaskConfig(String revisionRelTaskConfig) {
        this.revisionRelTaskConfig = revisionRelTaskConfig;
    }
}
