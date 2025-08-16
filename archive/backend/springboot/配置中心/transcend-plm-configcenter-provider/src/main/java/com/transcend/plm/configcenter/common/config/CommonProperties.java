package com.transcend.plm.configcenter.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 通用配置类
 *
 * @author luojie
 * @version: 1.0
 * @date 2020/12/26 17:10
 */
@Configuration
@ConfigurationProperties(prefix = "transcend.plm.common")
@Getter
@Setter
@ToString
public class CommonProperties {


    /**
     * 数据库in查询限制数量
     */
    private short dbInLimit = 1000;

}
