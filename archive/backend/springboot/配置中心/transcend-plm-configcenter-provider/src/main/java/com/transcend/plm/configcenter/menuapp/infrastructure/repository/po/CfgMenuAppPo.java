package com.transcend.plm.configcenter.menuapp.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

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
@TableName("cfg_menu_app")
public class CfgMenuAppPo extends BasePoEntity implements Serializable {


    /**
     * 父编码
     */
    private String parentBid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型编码（object-对象，view-视图，url-外链）
     */
    private String typeCode;

    /**
     * 类型值
     */
    private String typeValue;

    /**
     * 图标
     */
    private String icon;

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