package com.transcend.plm.datadriven.common.wapper;

import com.transcend.plm.datadriven.api.model.ObjectEnum;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 基础对象包装类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/17 14:46
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class TranscendObjectWrapper extends TranscendBaseWrapper {

    public TranscendObjectWrapper(@NotNull Map<String, Object> metadata) {
        super(metadata);
    }

    public TranscendObjectWrapper(@NonNull MapWrapper wrapper) {
        super(wrapper);
    }

    public String getLcTemplBid() {
        return this.getStr(ObjectEnum.LC_TEMPL_BID.getCode());
    }

    public TranscendObjectWrapper setLcTemplBid(String lcTemplBid) {
        this.put(ObjectEnum.LC_TEMPL_BID.getCode(), lcTemplBid);
        return this;
    }

    public String getLcTemplVersion() {
        return this.getStr(ObjectEnum.LC_TEMPL_VERSION.getCode());
    }

    public TranscendObjectWrapper setLcTemplVersion(String lcTemplVersion) {
        this.put(ObjectEnum.LC_TEMPL_VERSION.getCode(), lcTemplVersion);
        return this;
    }

    public String getLifeCycleCode() {
        return this.getStr(ObjectEnum.LIFE_CYCLE_CODE.getCode());
    }

    public TranscendObjectWrapper setLifeCycleCode(String lifeCycleCode) {
        this.put(ObjectEnum.LIFE_CYCLE_CODE.getCode(), lifeCycleCode);
        return this;
    }

    public String getName() {
        return this.getStr(ObjectEnum.NAME.getCode());
    }

    public TranscendObjectWrapper setName(String name) {
        this.put(ObjectEnum.NAME.getCode(), name);
        return this;
    }
    public String getCoding() {
        return this.getStr(ObjectEnum.CODING.getCode());
    }

    public TranscendObjectWrapper setCoding(String coding) {
        this.put(ObjectEnum.CODING.getCode(), coding);
        return this;
    }

    public String getLcModelCode() {
        return this.getStr(ObjectEnum.LC_MODEL_CODE.getCode());
    }

    public TranscendObjectWrapper setLcModelCode(String lcModelCode) {
        this.put(ObjectEnum.LC_MODEL_CODE.getCode(), lcModelCode);
        return this;
    }

    public String getForeignBid() {
        return this.getStr(ObjectEnum.FOREIGN_BID.getCode());
    }

    public TranscendObjectWrapper setForeignBid(String foreignBid) {
        this.put(ObjectEnum.FOREIGN_BID.getCode(), foreignBid);
        return this;
    }

    public String getPermissionBid() {
        return this.getStr(ObjectEnum.PERMISSION_BID.getCode());
    }

    public TranscendObjectWrapper setPermissionBid(String permissionBid) {
        this.put(ObjectEnum.PERMISSION_BID.getCode(), permissionBid);
        return this;
    }

    public String getSpaceBid() {
        return this.getStr(ObjectEnum.SPACE_BID.getCode());
    }

    public TranscendObjectWrapper setSpaceBid(String spaceBid) {
        this.put(ObjectEnum.SPACE_BID.getCode(), spaceBid);
        return this;
    }

    public String getSpaceAppBid() {
        return this.getStr(ObjectEnum.SPACE_APP_BID.getCode());
    }

    public TranscendObjectWrapper setSpaceAppBid(String appId) {
        this.put(ObjectEnum.SPACE_APP_BID.getCode(), appId);
        return this;
    }

    public String getModelCode() {
        return this.getStr(ObjectEnum.MODEL_CODE.getCode());
    }

    public TranscendObjectWrapper setModelCode(String modelCode) {
        this.put(ObjectEnum.MODEL_CODE.getCode(), modelCode);
        return this;
    }

}
