package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@ApiModel("对象实例模型")
@Data
public class MObject extends MBaseData {

    @ApiModelProperty("对象模型编码")
    private String modelCode;

    @ApiModelProperty("编码")
    private String coding;

    @ApiModelProperty("生命周期编码")
    private String lifeCycleCode;

    @ApiModelProperty("生命周期编码+对象模型编码组合 code(权限过滤使用)")
    private String lcModelCode;

    @ApiModelProperty("绑定的生命周期模板bid")
    private String lcTemplBid;

    @ApiModelProperty("绑定的生命周期模板对应版本")
    private String lcTemplVersion;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("外部唯一标识")
    private String foreignBid;
    private String permissionBid;

    /**
     * @return {@link String }
     */
    public String getModelCode() {
        return (String) this.get(ObjectEnum.MODEL_CODE.getCode());
    }

    /**
     * @param modelCode
     * @return {@link MObject }
     */
    public MObject setModelCode(String modelCode) {
        this.put(ObjectEnum.MODEL_CODE.getCode(), modelCode);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getLcTemplBid() {
        return (String) this.get(ObjectEnum.LC_TEMPL_BID.getCode());
    }

    /**
     * @param lcTemplBid
     * @return {@link MObject }
     */
    public MObject setLcTemplBid(String lcTemplBid) {
        this.put(ObjectEnum.LC_TEMPL_BID.getCode(), lcTemplBid);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getLcTemplVersion() {
        return (String) this.get(ObjectEnum.LC_TEMPL_VERSION.getCode());
    }

    /**
     * @param lcTemplVersion
     * @return {@link MObject }
     */
    public MObject setLcTemplVersion(String lcTemplVersion) {
        this.put(ObjectEnum.LC_TEMPL_VERSION.getCode(), lcTemplVersion);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getLifeCycleCode() {
        return (String) this.get(ObjectEnum.LIFE_CYCLE_CODE.getCode());
    }

    /**
     * @param lifeCycleCode
     * @return {@link MObject }
     */
    public MObject setLifeCycleCode(String lifeCycleCode) {
        this.put(ObjectEnum.LIFE_CYCLE_CODE.getCode(), lifeCycleCode);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getLcModelCode() {
        return (String) this.get(ObjectEnum.LC_MODEL_CODE.getCode());
    }

    /**
     * @return {@link String }
     */
    public String getName() {
        return (String) this.get(ObjectEnum.NAME.getCode());
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.put(ObjectEnum.NAME.getCode(), name);
    }


    /**
     * @param coding  coding
     * @return {@link MObject }
     */
    public MObject getCoding(String coding) {
        this.put(ObjectEnum.CODING.getCode(), coding);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getCoding() {
        return (String) this.get(ObjectEnum.CODING.getCode());
    }

    /**
     * @param lcModelCode
     * @return {@link MObject }
     */
    public MObject setLcModelCode(String lcModelCode) {
        this.put(ObjectEnum.LC_MODEL_CODE.getCode(), lcModelCode);
        return this;
    }

    /**
     * @param foreignBid
     */
    public void setForeignBid(String foreignBid) {
        this.put(ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
    }

    /**
     * @return {@link String }
     */
    public String getForeignBid() {
        return (String) this.get(ObjectEnum.FOREIGN_BID.getCode());
    }

    /**
     * @param permissionBid
     */
    @Override
    public void setPermissionBid(String permissionBid) {
        this.put(ObjectEnum.PERMISSION_BID.getCode(), permissionBid);
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getPermissionBid() {
        return (String) this.get(ObjectEnum.PERMISSION_BID.getCode());
    }

    /**
     * 转换
     *
     * @param mBaseData m
     * @return this
     */
    public static MObject upcast(MBaseData mBaseData) {
        MObject mObject = new MObject();
        mObject.setBid(mBaseData.getBid())
                .setCreatedBy(mBaseData.getCreatedBy())
                .setCreatedTime(mBaseData.getCreatedTime())
                .setUpdatedBy(mBaseData.getUpdatedBy())
                .setUpdatedTime(mBaseData.getUpdatedTime())
                .setDeleteFlag(mBaseData.getDeleteFlag())
                .setEnableFlag(mBaseData.getEnableFlag())
                .setTenantId(mBaseData.getTenantId());
        return mObject;
    }
}
