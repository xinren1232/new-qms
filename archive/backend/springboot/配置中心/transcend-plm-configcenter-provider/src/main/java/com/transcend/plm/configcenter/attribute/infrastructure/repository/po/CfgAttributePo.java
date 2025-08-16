package com.transcend.plm.configcenter.attribute.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 属性表
 * @TableName cfg_attribute
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Data
@TableName("cfg_attribute")
public class CfgAttributePo extends BasePoEntity implements Serializable {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 对象模型code
     */
    private String modelCode;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 分组
     */
    private String groupName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据库字段名称
     */
    private String dbKey;

    /**
     * 是否为空
     */
    private Boolean nullAble;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 国际化语言字典
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, String> langDict;

    private static final long serialVersionUID = 1L;

}