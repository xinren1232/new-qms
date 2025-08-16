package com.transcend.plm.datadriven.common.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.BaseEntity;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-23 09:53
 **/
public class BasePoEntity extends BaseEntity<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "bid", fill = FieldFill.INSERT)
    private String bid ;

    @TableField(value = "enable_flag",fill = FieldFill.INSERT)
    /**
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    /**
     * 是否删除（0-未删除；1已删除）
     */
    @TableField(value = "delete_flag")
    @TableLogic
    private Integer deleteFlag=0;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    @Override
    public Integer getEnableFlag() {
        return enableFlag;
    }

    @Override
    public void setEnableFlag(Integer enableFlag) {
        this.enableFlag = enableFlag;
    }

    @Override
    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    @Override
    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}
