package com.transcend.plm.datadriven.apm.springframework.config;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * 空间应用视图属性
 *
 * @author luojie
 * @version: 1.0
 * @date 2021/03/26 17:10
 */
@Configuration
@ConfigurationProperties(prefix = "transcend.plm.apm.space.app")
@Getter
@Setter
@ToString
public class SpaceAppViewProperties {

    /**
     * 多对象链
     */
    private List<String> multiObjectChain = Lists.newArrayList("AAA","AAA001","AAA002","AAB");
}
