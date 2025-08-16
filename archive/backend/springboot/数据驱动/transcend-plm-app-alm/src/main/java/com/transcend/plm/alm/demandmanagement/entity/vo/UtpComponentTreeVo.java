package com.transcend.plm.alm.demandmanagement.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * utp组件模块树对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/30 10:36
 */
@Data
public class UtpComponentTreeVo implements Serializable {

    /**
     * 名称
     */
    @JsonProperty("name")
    private String name;
    /**
     * 编码
     */
    @JsonProperty("code")
    private String code;
    /**
     * 父层级编码
     */
    @JsonProperty("parentCode")
    private String parentCode;

    /**
     * 模块负责人
     */
    @JsonProperty("ownerName")
    private String ownerName;
    /**
     * 模块负责人
     */
    @JsonProperty("ownerCode")
    private String ownerCode;
    /**
     * 类型
     * group 领域
     * subGroup 子领域
     * component 模块
     * subComponent 子模块
     */
    @JsonProperty("type")
    private String type;

    /**
     * 子层数据
     */
    @JsonProperty("children")
    private List<UtpComponentTreeVo> children;

    /**
     * 是否删除
     */
    @JsonProperty("disabled")
    private Boolean disabled;

}
