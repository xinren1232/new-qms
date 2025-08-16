package com.transcend.plm.datadriven.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 通用配置类
 *
 * @author luojie
 * @date 2020/12/26 17:10
 */
@Configuration
@ConfigurationProperties(prefix = "transcend.plm.object")
@Getter
@Setter
@ToString
public class ObjectProperties {


    /**
     * 扩展支持列集合
     */
    private List<String> extSupportColumns = Arrays.asList("front_created_by_user_name", "front_object_name");

}
