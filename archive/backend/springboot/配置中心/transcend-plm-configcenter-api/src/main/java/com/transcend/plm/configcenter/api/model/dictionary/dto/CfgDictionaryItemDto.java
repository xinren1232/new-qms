package com.transcend.plm.configcenter.api.model.dictionary.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 
 * 公用字典赋值表
 */
@ToString
public class CfgDictionaryItemDto extends HashMap<String, Object> implements Serializable {

    /**
     * 关联业务id(dictionary的bid)
     */
    private String dictionaryBid;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 父bid
     */
    private String parentBid;

    /**
     * 字段key
     */
    @ApiModelProperty(value = "排序号", example = "1", required = false)
    private String keyCode;


    /**
     * 多语言值 map
     */
    @ApiModelProperty(value = "multilingualValueMap", example = "{\"rus\":\"Как ты\",\"es\":\"Qué tal\"}", required = false)
    private Map<String, String> multilingualValueMap;


    /**
     * 前端样式
     */
    @ApiModelProperty(value = "前端样式", example = "cole:red", required = false)
    private String style;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述", example = "xx", required = false)
    private String description;

    /**
     * 0未启用 1启用  2禁用
     */
    @ApiModelProperty(value = "0未启用 1启用  2禁用", example = "enable", required = false)
    private Integer enableFlag;

    /**
     * 自定义属性1
     */
    private String custom1;

    /**
     * 自定义属性2
     */
    private String custom2;

    /**
     * 自定义属性3
     */
    private String custom3;

    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号", example = "1", required = false)
    private Integer sort;

    /**
     * 插入时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新用户
     */
    private String updatedBy;

    public static CfgDictionaryItemDto of(){
        return new CfgDictionaryItemDto();
    }

    private static final long serialVersionUID = 1L;


    public String getBid() {
        return (String) get("bid");
    }

    public void setBid(String bid) {
        this.bid = bid;
        this.put("bid", bid);
    }
    public String getDictionaryBid() {
        return (String) get("dictionaryBid");
    }

    public void setDictionaryBid(String dictionaryBid) {
        this.dictionaryBid = dictionaryBid;
        this.put("dictionaryBid", dictionaryBid);
    }

    public String getParentBid() {
        return (String) get("parentBid");
    }

    public void setParentBid(String parentBid) {
        this.parentBid = parentBid;
        this.put("parentBid", parentBid);
    }

    public String getKeyCode() {
        return (String) get("keyCode");
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
        this.put("keyCode", keyCode);
    }

    public Map<String, String> getMultilingualValueMap() {
        return multilingualValueMap;
    }

    public void setMultilingualValueMap(Map<String, String> multilingualValueMap) {
        this.multilingualValueMap = multilingualValueMap;
    }

    public String getDescription() {
        return (String) get("description");
    }

    public void setDescription(String description) {
        this.description = description;
        this.put("description", description);
    }

    public Integer getEnableFlag() {
        return (Integer) get("enableFlag");
    }

    public void setEnableFlag(Integer enableFlag) {
        this.enableFlag = enableFlag;
        this.put("enableFlag", enableFlag);
    }

    public String getCreatedBy() {
        return (String) get("createdBy");
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getSort() {
        return (Integer)get("sort");
    }

    public void setSort(Integer sort) {
        this.sort = sort;
        this.put("sort", sort);
    }

    public String getCustom1() {
        return (String) get("custom1");
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
        this.put("custom1", custom1);
    }

    public String getCustom2() {
        return (String) get("custom2");
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
        this.put("custom2", custom2);
    }

    public String getCustom3() {
        return (String) get("custom3");
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
        this.put("custom3", custom3);
    }
}