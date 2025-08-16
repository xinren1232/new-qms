package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * 对象关系模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
public class MRelationObject extends MBaseData{

    @ApiModelProperty("模型Code")
    private String modelCode;

    @ApiModelProperty("源BID")
    private String sourceBid;

    @ApiModelProperty("源数据BID")
    private String sourceDataBid;

    @ApiModelProperty("目标BID")
    private String targetBid;

    @ApiModelProperty("目标数据BID")
    private String targetDataBid;

    @ApiModelProperty("是否草稿")
    private boolean draft;

    private String permissionBid;

    @ApiModelProperty("关联行为")
    private String relBehavior;

    /**
     * @return {@link String }
     */
    public String getModelCode() {
        return String.valueOf(this.get(RelationObjectEnum.MODEL_CODE.getCode()));
    }

    /**
     * @param modelCode
     */
    public void setModelCode(String modelCode) {
        this.put(RelationObjectEnum.MODEL_CODE.getCode(),modelCode);
    }

    /**
     * @return {@link String }
     */
    public String getSourceBid() {
        return String.valueOf(this.get(RelationObjectEnum.SOURCE_BID.getCode()));
    }

    /**
     * @param sourceBid
     */
    public void setSourceBid(String sourceBid) {
        this.put(RelationObjectEnum.SOURCE_BID.getCode(),sourceBid);
    }

    /**
     * @return {@link String }
     */
    public String getSourceDataBid() {
        return String.valueOf(this.get(RelationObjectEnum.SOURCE_DATA_BID.getCode()));
    }

    /**
     * @param sourceDataBid
     */
    public void setSourceDataBid(String sourceDataBid) {
        this.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(),sourceDataBid);
    }

    /**
     * @return {@link String }
     */
    public String getTargetBid() {
        return String.valueOf(this.get(RelationObjectEnum.TARGET_BID.getCode()));
    }

    /**
     * @param targetBid
     */
    public void setTargetBid(String targetBid) {
        this.put(RelationObjectEnum.TARGET_BID.getCode(),targetBid);
    }

    /**
     * @return {@link String }
     */
    public String getTargetDataBid() {
        return String.valueOf(this.get(RelationObjectEnum.TARGET_DATA_BID.getCode()));
    }

    /**
     * @param targetDataBid
     */
    public void setTargetDataBid(String targetDataBid) {
        this.put(RelationObjectEnum.TARGET_DATA_BID.getCode(),targetDataBid);
    }

    /**
     * @return boolean
     */
    public boolean isDraft() {
        return (boolean) this.get(RelationObjectEnum.DRAFT.getCode());
    }

    /**
     * @param draft
     */
    public void setDraft(boolean draft) {
        this.put(RelationObjectEnum.DRAFT.getCode(), draft);
    }

    /**
     * @param permissionBid
     */
    @Override
    public void setPermissionBid(String permissionBid) {
        this.put(RelationObjectEnum.PERMISSION_BID.getCode(),permissionBid);
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getPermissionBid() {
        return String.valueOf(this.get(RelationObjectEnum.PERMISSION_BID.getCode()));
    }

    /**
     * @return {@link String }
     */
    public String getRelBehavior() {
        return String.valueOf(this.get(RelationObjectEnum.REL_BEHAVIOR.getCode()));
    }

    /**
     * @param relBehavior
     */
    public void setRelBehavior(String relBehavior) {
        this.put(RelationObjectEnum.REL_BEHAVIOR.getCode(), relBehavior);
    }
}
