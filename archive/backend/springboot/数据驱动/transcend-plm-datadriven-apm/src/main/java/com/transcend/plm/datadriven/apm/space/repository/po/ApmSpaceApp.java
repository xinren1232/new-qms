package com.transcend.plm.datadriven.apm.space.repository.po;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 空间下对象应用表
 * @author unknown
 * @TableName apm_space_object
 */
@TableName(value ="apm_space_app")
@Data
public class ApmSpaceApp implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间业务id
     */
    private String spaceBid;

    /**
     * 领域Bid
     */
    private String sphereBid;


    /**
     * 类型code
     */
    private String typeCode;
    /**
     * 类型
     */
    private String type;
    /**
     * 对象模型code
     */
    private String modelCode;

    /**
     * 对象应用名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标志
     */
    private Integer deleteFlag;

    /**
     * 启用标识
     */
    private Integer enableFlag;

    /**
     * 是否显示
     */
    private Integer visibleFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 图标url
     */
    private String iconUrl;

    /**
     * 扩展字段
     */
    private JSONObject configContent;

    /**
     * 是否是版本对象
     */
    private Boolean isVersionObject;

    /**
     * 复制实例标识（空:不复制，APP_INSTANCE:默认复制应用实例)
     */
    private String copyInstanceModel;

    /**
     * 打开模式：空：默认打开列表，APP_INSTANCE:默认打开应用实例
     */
    private String openModel;
    /**
     * 分组名称
     */
    private String groupName;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApmSpaceApp that = (ApmSpaceApp) o;
        return Objects.equals(id, that.id) && Objects.equals(bid, that.bid) && Objects.equals(spaceBid, that.spaceBid) && Objects.equals(sphereBid, that.sphereBid) && Objects.equals(typeCode, that.typeCode) && Objects.equals(type, that.type) && Objects.equals(modelCode, that.modelCode) && Objects.equals(name, that.name) && Objects.equals(sort, that.sort) && Objects.equals(createdTime, that.createdTime) && Objects.equals(updatedTime, that.updatedTime) && Objects.equals(deleteFlag, that.deleteFlag) && Objects.equals(enableFlag, that.enableFlag) && Objects.equals(visibleFlag, that.visibleFlag) && Objects.equals(createdBy, that.createdBy) && Objects.equals(updatedBy, that.updatedBy) && Objects.equals(tenantId, that.tenantId) && Objects.equals(iconUrl, that.iconUrl) && Objects.equals(configContent, that.configContent) && Objects.equals(isVersionObject, that.isVersionObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bid, spaceBid, sphereBid, typeCode, type, modelCode, name, sort, createdTime, updatedTime, deleteFlag, enableFlag, visibleFlag, createdBy, updatedBy, tenantId, iconUrl, configContent, isVersionObject, copyInstanceModel, openModel);
    }

}