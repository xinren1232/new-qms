package com.transcend.plm.datadriven.common.wapper;

import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.RelationObjectEnum;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 关系对象包装器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 11:46
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class TranscendRelationWrapper extends TranscendBaseWrapper {

    public TranscendRelationWrapper(@NotNull Map<String, Object> metadata) {
        super(metadata);
    }

    public TranscendRelationWrapper(@NonNull MapWrapper wrapper) {
        super(wrapper);
    }

    public String getModelCode() {
        return this.getStr(RelationObjectEnum.MODEL_CODE.getCode());
    }

    public TranscendRelationWrapper setModelCode(String modelCode) {
        this.put(RelationObjectEnum.MODEL_CODE.getCode(), modelCode);
        return this;
    }

    public String getSourceBid() {
        return this.getStr(RelationObjectEnum.SOURCE_BID.getCode());
    }

    public TranscendRelationWrapper setSourceBid(String sourceBid) {
        this.put(RelationObjectEnum.SOURCE_BID.getCode(), sourceBid);
        return this;
    }

    public String getTargetBid() {
        return this.getStr(RelationObjectEnum.TARGET_BID.getCode());
    }

    public TranscendRelationWrapper setTargetBid(String targetBid) {
        this.put(RelationObjectEnum.TARGET_BID.getCode(), targetBid);
        return this;
    }

    public String getSourceDataBid() {
        return this.getStr(RelationObjectEnum.SOURCE_DATA_BID.getCode());
    }

    public TranscendRelationWrapper setSourceDataBid(String sourceDataBid) {
        this.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(), sourceDataBid);
        return this;
    }

    public String getTargetDataBid() {
        return this.getStr(RelationObjectEnum.TARGET_DATA_BID.getCode());
    }

    public TranscendRelationWrapper setTargetDataBid(String targetDataBid) {
        this.put(RelationObjectEnum.TARGET_DATA_BID.getCode(), targetDataBid);
        return this;
    }

    public Boolean getDraft() {
        return this.getBool(RelationObjectEnum.DRAFT.getCode());
    }

    public TranscendRelationWrapper setDraft(Boolean draft) {
        this.put(RelationObjectEnum.DRAFT.getCode(), draft);
        return this;
    }

    public String getPermissionBid() {
        return this.getStr(RelationObjectEnum.PERMISSION_BID.getCode());
    }

    public TranscendRelationWrapper setPermissionBid(String permissionBid) {
        this.put(RelationObjectEnum.PERMISSION_BID.getCode(), permissionBid);
        return this;
    }

    public String getRelBehavior() {
        return this.getStr(RelationObjectEnum.REL_BEHAVIOR.getCode());
    }

    public TranscendRelationWrapper setRelBehavior(String relBehavior) {
        this.put(RelationObjectEnum.REL_BEHAVIOR.getCode(), relBehavior);
        return this;
    }

    public String getSpaceBid() {
        return this.getStr(ObjectEnum.SPACE_BID.getCode());
    }

    public TranscendRelationWrapper setSpaceBid(String spaceBid) {
        this.put(ObjectEnum.SPACE_BID.getCode(), spaceBid);
        return this;
    }

    public String getSpaceAppBid() {
        return this.getStr(ObjectEnum.SPACE_APP_BID.getCode());
    }

    public TranscendRelationWrapper setSpaceAppBid(String appId) {
        this.put(ObjectEnum.SPACE_APP_BID.getCode(), appId);
        return this;
    }

}
