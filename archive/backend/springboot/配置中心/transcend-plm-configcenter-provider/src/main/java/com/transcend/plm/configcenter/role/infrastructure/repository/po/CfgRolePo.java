package com.transcend.plm.configcenter.role.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.BaseEntity;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
@TableName("cfg_role")
public class CfgRolePo extends BasePoEntity implements Serializable {


    /**
     * 父编码
     */
    private String parentBid;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;


    private static final long serialVersionUID = 1L;

}