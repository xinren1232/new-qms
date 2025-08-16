package com.transcend.plm.datadriven.common.wapper;

import com.transcend.plm.datadriven.api.model.ObjectTreeEnum;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 树对象包装
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/23 15:58
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class TranscendTreeObjectWrapper extends TranscendObjectWrapper {


    public TranscendTreeObjectWrapper(@NotNull Map<String, Object> metadata) {
        super(metadata);
    }

    public TranscendTreeObjectWrapper(@NonNull MapWrapper wrapper) {
        super(wrapper);
    }

    public String getParentBid() {
        return this.getStr(ObjectTreeEnum.PARENT_BID.getCode());
    }

    public TranscendBaseWrapper setParentBid(String parentBid) {
        this.put(ObjectTreeEnum.PARENT_BID.getCode(), parentBid);
        return this;
    }

    public List<TranscendTreeObjectWrapper> getChildren() {
        return this.getWrapperList(TranscendTreeObjectWrapper.class, ObjectTreeEnum.CHILDREN.getCode());
    }

    public TranscendBaseWrapper setChildren(List<TranscendTreeObjectWrapper> children) {
        this.put(ObjectTreeEnum.CHILDREN.getCode(), children);
        return this;
    }

    public Integer getSort() {
        return this.getInt(ObjectTreeEnum.SORT.getCode());
    }

    public TranscendBaseWrapper setSort(Integer sort) {
        this.put(ObjectTreeEnum.SORT.getCode(), sort);
        return this;
    }

    public String getRelInstanceBid() {
        return this.getStr(ObjectTreeEnum.REL_INSTANCE_BID.getCode());
    }

    public TranscendBaseWrapper setRelInstanceBid(String relInstanceBid) {
        this.put(ObjectTreeEnum.REL_INSTANCE_BID.getCode(), relInstanceBid);
        return this;
    }

    public Boolean getChecked() {
        return this.getBool(ObjectTreeEnum.CHECKED.getCode());
    }

    public TranscendBaseWrapper setChecked(Boolean checked) {
        this.put(ObjectTreeEnum.CHECKED.getCode(), checked);
        return this;
    }

    public String getTreeBid() {
        return this.getStr(ObjectTreeEnum.TREE_BID.getCode());
    }

    public TranscendBaseWrapper setTreeBid(String treeBid) {
        this.put(ObjectTreeEnum.TREE_BID.getCode(), treeBid);
        return this;
    }

}
