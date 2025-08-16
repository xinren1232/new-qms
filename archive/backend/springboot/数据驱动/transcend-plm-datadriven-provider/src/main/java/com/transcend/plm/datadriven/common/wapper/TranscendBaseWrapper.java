package com.transcend.plm.datadriven.common.wapper;

import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 元数据包装类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/17 14:49
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class TranscendBaseWrapper extends MapWrapper {

    public TranscendBaseWrapper(@NotNull Map<String, Object> metadata) {
        super(metadata);
    }

    public TranscendBaseWrapper(@NonNull MapWrapper wrapper) {
        super(wrapper);
    }

    public String getBid() {
        return this.getStr(BaseDataEnum.BID.getCode());
    }

    public TranscendBaseWrapper setBid(String bid) {
        this.put(BaseDataEnum.BID.getCode(), bid);
        return this;
    }

    public String getTenantId() {
        return this.getStr(BaseDataEnum.TENANT_ID.getCode());
    }

    public TranscendBaseWrapper setTenantId(String tenantId) {
        this.put(BaseDataEnum.TENANT_ID.getCode(), tenantId);
        return this;
    }

    public Boolean getDeleteFlag() {
        return this.getBool(BaseDataEnum.DELETE_FLAG.getCode());
    }

    public TranscendBaseWrapper setDeleteFlag(Boolean deleteFlag) {
        this.put(BaseDataEnum.DELETE_FLAG.getCode(), deleteFlag);
        return this;
    }

    public Boolean getEnableFlag() {
        return this.getBool(BaseDataEnum.ENABLE_FLAG.getCode());
    }

    public TranscendBaseWrapper setEnableFlag(Boolean enableFlag) {
        this.put(BaseDataEnum.ENABLE_FLAG.getCode(), enableFlag);
        return this;
    }

    public String getCreatedBy() {
        return this.getStr(BaseDataEnum.CREATED_BY.getCode());
    }

    public TranscendBaseWrapper setCreatedBy(String createdBy) {
        this.put(BaseDataEnum.CREATED_BY.getCode(), createdBy);
        return this;
    }

    public LocalDateTime getCreatedTime() {
        return this.getLocalDateTime(BaseDataEnum.CREATED_TIME.getCode());
    }

    public TranscendBaseWrapper setCreatedTime(LocalDateTime createdTime) {
        this.put(BaseDataEnum.CREATED_TIME.getCode(), createdTime);
        return this;
    }

    public String getUpdatedBy() {
        return this.getStr(BaseDataEnum.UPDATED_BY.getCode());
    }

    public TranscendBaseWrapper setUpdatedBy(String updatedBy) {
        this.put(BaseDataEnum.UPDATED_BY.getCode(), updatedBy);
        return this;
    }

    public LocalDateTime getUpdatedTime() {
        return this.getLocalDateTime(BaseDataEnum.UPDATED_TIME.getCode());
    }

    public TranscendBaseWrapper setUpdatedTime(LocalDateTime updatedTime) {
        this.put(BaseDataEnum.UPDATED_TIME.getCode(), updatedTime);
        return this;
    }

    public Long getId() {
        return this.getLong(BaseDataEnum.ID.getCode());
    }

    public TranscendBaseWrapper setId(String id) {
        this.put(BaseDataEnum.ID.getCode(), id);
        return this;
    }

}
