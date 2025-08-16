package com.transcend.plm.alm.demandmanagement.config;

import com.transcend.plm.alm.demandmanagement.service.RelationUnlinkInterceptorService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author bin.yin
 * @description 需求管理配置项
 * @date 2024/06/21 11:47
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "transcend.demand.management")
public class DemandManagementProperties {

    /**
     * 当前处理人
     **/
    private String currentHandler = "currentHandler";

    /**
     * 应用modelCode
     **/
    private String appModelCode = "A4G";


    /**
     * RRmodelCode
     **/
    private String rrModelCode = "A5E";

    /**
     * 模型状态配置
     */
    private Map<String, String[]> liefCycleCode = Collections.emptyMap();

    /**
     * 关系解除拦截配置
     */
    private List<RelationUnlinkInterceptorService.Config> relationUnlinkInterceptor = Collections.emptyList();

}
