package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

/**
 * 基础模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@ApiModel("基础实例模型")
public class MBaseData extends HashMap<String, Object> {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("业务ID")
    private String bid;

    @ApiModelProperty("租户")
    private Long tenantId;

    @ApiModelProperty("创建时间")
    private Date createdTime;

    @ApiModelProperty("更新时间")
    private Date updatedTime;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("更新人")
    private String updatedBy;

    @ApiModelProperty("有效标识")
    private Boolean enableFlag;

    @ApiModelProperty("删除标识")
    private Boolean deleteFlag;

    private String permissionBid;


    /**
     * @return {@link String }
     */
    public String getBid() {
        return (String) this.get(BaseDataEnum.BID.getCode());
    }

    /**
     * @param bid
     * @return {@link MBaseData }
     */
    public MBaseData setBid(String bid) {
        this.put(BaseDataEnum.BID.getCode(), bid);
        return this;
    }

    /**
     * @return {@link Long }
     */
    public Long getTenantId() {
        return (Long) this.get(BaseDataEnum.TENANT_ID.getCode());
    }

    /**
     * @param tenantId
     * @return {@link MBaseData }
     */
    public MBaseData setTenantId(Long tenantId) {
        this.put(BaseDataEnum.TENANT_ID.getCode(), tenantId);
        return this;
    }

    /**
     * @return {@link Boolean }
     */
    public Boolean getDeleteFlag() {
        return (Boolean) this.get(BaseDataEnum.DELETE_FLAG.getCode());
    }

    /**
     * @param deleteFlag
     * @return {@link MBaseData }
     */
    public MBaseData setDeleteFlag(Boolean deleteFlag) {
        this.put(BaseDataEnum.DELETE_FLAG.getCode(), deleteFlag);
        return this;
    }

    /**
     * @return {@link Boolean }
     */
    public Boolean getEnableFlag() {
        return (Boolean) this.get(BaseDataEnum.ENABLE_FLAG.getCode());
    }

    /**
     * @param enableFlag
     * @return {@link MBaseData }
     */
    public MBaseData setEnableFlag(Boolean enableFlag) {
        this.put(BaseDataEnum.ENABLE_FLAG.getCode(), enableFlag);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getCreatedBy() {
        return (String) this.get(BaseDataEnum.CREATED_BY.getCode());
    }

    /**
     * @param createdBy
     * @return {@link MBaseData }
     */
    public MBaseData setCreatedBy(String createdBy) {
        this.put(BaseDataEnum.CREATED_BY.getCode(), createdBy);
        return this;
    }

    /**
     * @return {@link LocalDateTime }
     */
    public LocalDateTime getCreatedTime() {
        return (LocalDateTime) this.get(BaseDataEnum.CREATED_TIME.getCode());
    }

    /**
     * @param createdTime
     * @return {@link MBaseData }
     */
    public MBaseData setCreatedTime(LocalDateTime createdTime) {
        this.put(BaseDataEnum.CREATED_TIME.getCode(), createdTime);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getUpdatedBy() {
        return (String) this.get(BaseDataEnum.UPDATED_BY.getCode());
    }

    /**
     * @param updatedBy
     * @return {@link MBaseData }
     */
    public MBaseData setUpdatedBy(String updatedBy) {
        this.put(BaseDataEnum.UPDATED_BY.getCode(), updatedBy);
        return this;
    }

    /**
     * @return {@link LocalDateTime }
     */
    public LocalDateTime getUpdatedTime() {
        return (LocalDateTime) this.get(BaseDataEnum.UPDATED_TIME.getCode());
    }

    /**
     * @param updatedTime
     * @return {@link MBaseData }
     */
    public MBaseData setUpdatedTime(LocalDateTime updatedTime) {
        this.put(BaseDataEnum.UPDATED_TIME.getCode(), updatedTime);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getId() {
        return (String) this.get(BaseDataEnum.ID.getCode());
    }

    /**
     * @param id
     * @return {@link MBaseData }
     */
    public MBaseData setId(String id) {
        this.put(BaseDataEnum.ID.getCode(), id);
        return this;
    }

    public void setPermissionBid(String permissionBid) {
        this.put(ObjectEnum.PERMISSION_BID.getCode(), permissionBid);
    }

    /**
     * @return {@link String }
     */
    public String getPermissionBid() {
        return (String) this.get(ObjectEnum.PERMISSION_BID.getCode());
    }
}
