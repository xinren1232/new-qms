package com.transcend.plm.configcenter.api.model.permission.qo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 15:43
 **/
@Data
@ApiModel("标准属性")
public class CfgObjectPermissionOperationQo {
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
     * 使用状态，禁用2，启用1，未启用0(默认0)
     */
    private Integer enableFlag;

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
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
