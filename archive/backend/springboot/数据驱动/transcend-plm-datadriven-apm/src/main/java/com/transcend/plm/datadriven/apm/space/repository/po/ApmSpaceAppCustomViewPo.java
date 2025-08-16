package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.datadriven.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author unknown
 * @TableName apm_space_app_custom_view
 */
@TableName(value = "apm_space_app_custom_view", autoResultMap = true)
@Data
public class ApmSpaceAppCustomViewPo extends BasePoEntity implements Serializable {


    /**
     * 业务id
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 空间应用bid
     */
    @TableField(value = "space_app_bid")
    private String spaceAppBid;
    /**
     * 编码
     */
    @TableField(value = "category")
    private String category;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 配置内容
     */
    @TableField(value = "config_content", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> configContent;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;
    /**
     * 权限类型（1-指定团队人员，2-所有空间团队成员）
     */
    @TableField(value = "permission_type")
    private Byte permissionType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}