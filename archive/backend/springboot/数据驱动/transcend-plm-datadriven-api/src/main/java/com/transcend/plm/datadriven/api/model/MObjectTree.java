package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 树对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
public class MObjectTree extends MObject {

    @ApiModelProperty("父对象bid")
    private String parentBid;

    @ApiModelProperty("子集合")
    private List<MObjectTree> children;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("是否选中")
    private boolean checked;

    @ApiModelProperty("关系实例bid")
    private String relInstanceBid;

    @ApiModelProperty("多对象树bid")

    private String treeBid;

    /**
     * @return {@link String }
     */
    public String getRelInstanceBid() {
        return (String) this.get(ObjectTreeEnum.REL_INSTANCE_BID.getCode());
    }

    /**
     * @param relInstanceBid
     * @return {@link MObjectTree }
     */
    public MObjectTree setRelInstanceBid(String relInstanceBid) {
        this.put(ObjectTreeEnum.REL_INSTANCE_BID.getCode(), relInstanceBid);
        return this;
    }

    /**
     * @return boolean
     */
    public boolean getChecked() {
        if(this.get(ObjectTreeEnum.CHECKED.getCode()) == null){
            return false;
        }
        return (Boolean) this.get(ObjectTreeEnum.CHECKED.getCode());
    }

    /**
     * @param checked
     * @return {@link MObjectTree }
     */
    public MObjectTree setChecked(boolean checked) {
        this.put(ObjectTreeEnum.CHECKED.getCode(), checked);
        return this;
    }

    /**
     * @return {@link String }
     */
    public String getParentBid() {
        return (String) this.get(ObjectTreeEnum.PARENT_BID.getCode());
    }

    /**
     * @param parentBid
     * @return {@link MObjectTree }
     */
    public MObjectTree setParentBid(String parentBid) {
        this.put(ObjectTreeEnum.PARENT_BID.getCode(), parentBid);
        return this;
    }

    /**
     * @return {@link List }<{@link MObjectTree }>
     */
    public List<MObjectTree> getChildren() {
        return (List<MObjectTree>) this.get(ObjectTreeEnum.CHILDREN.getCode());
    }

    /**
     * @param children
     * @return {@link MObjectTree }
     */
    public MObjectTree setChildren(List<MObjectTree> children) {
        this.put(ObjectTreeEnum.CHILDREN.getCode(), children);
        return this;
    }

    /**
     * @return {@link Integer }
     */
    public Integer getSort() {
        return (Integer) Optional.ofNullable(this.get(ObjectTreeEnum.SORT.getCode())).orElse(1);
    }

    /**
     * @param sort
     * @return {@link MObjectTree }
     */
    public MObjectTree setSort(Integer sort) {
        this.put(ObjectTreeEnum.SORT.getCode(), sort);
        return this;
    }

    public List<String> getTreeBid() {
        Object object = this.get(ObjectTreeEnum.TREE_BID.getCode());
        if (object instanceof List) {
            return ((List<?>) object).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
    }

    public MObjectTree setTreeBid(List<String> treeBid) {
        this.put(ObjectTreeEnum.TREE_BID.getCode(), treeBid);
        return this;
    }
}
