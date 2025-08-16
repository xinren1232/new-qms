package com.transcend.plm.datadriven.apm.springframework.config;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 全局角色配置
 *
 * @author luojie
 * @version: 1.0
 * @date 2021/03/26 17:10
 */
@Configuration
@ConfigurationProperties(prefix = "transcend.plm.apm.global-role")
@Getter
@Setter
@ToString
public class GlobalRoleProperties {

    /**
     * 管理员 administrators
     */
    private Set<String> administrators = Sets.newHashSet("18645974");
}
