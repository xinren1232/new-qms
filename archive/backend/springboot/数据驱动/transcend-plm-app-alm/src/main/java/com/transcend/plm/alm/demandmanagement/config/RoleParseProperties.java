package com.transcend.plm.alm.demandmanagement.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;


/**
 * * @description 角色解析配置项
 * @date 2024/06/21 11:47
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "transcend.role.parse")
public class RoleParseProperties {

    private String modelCodeAttrRoleMap;

    public Map<String, Map<String, String>> getModelCodeRoleMap() {
        return  JSON.parseObject(modelCodeAttrRoleMap, new TypeReference<Map<String, Map<String, String>>>() {
        });
    }


}
