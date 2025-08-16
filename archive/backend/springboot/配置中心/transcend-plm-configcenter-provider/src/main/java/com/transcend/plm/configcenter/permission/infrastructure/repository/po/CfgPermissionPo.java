package com.transcend.plm.configcenter.permission.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性表
 * @TableName cfg_attribute
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Data
@TableName("cfg_object_operation")
public class CfgPermissionPo extends BasePoEntity implements Serializable {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

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

    private static final long serialVersionUID = 1L;

}