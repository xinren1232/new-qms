package com.transcend.plm.configcenter.dictionary.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 
 * 公用字典赋值表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("cfg_dictionary_item")
public class CfgDictionaryItemPo extends BasePoEntity implements Serializable {

    /**
     * 关联业务id(common_dictionary的bid)
     */
    private String dictionaryBid;

    /**
     * 父bid
     */
    private String parentBid;

    /**
     * 字段key
     */
    private String keyCode;

    /**
     * 多语言值 map
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, String> multilingualValueMap;

    /**
     * 描述
     */
    private String description;


    /**
     * 自定义属性1
     */
    private String custom1;

    /**
     * 自定义属性2
     */
    private String custom2;


    /**
     * 自定义属性3
     */
    private String custom3;

    /**
     * 前端样式
     */
    private String style;

    /**
     * 排序号
     */
    private Integer sort;

    private static final long serialVersionUID = 1L;
}