package com.transcend.plm.configcenter.view.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 视图同步记录表
 *
 * @author xin.wu2
 * TableName cfg_view_sync_record
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cfg_view_sync_record", autoResultMap = true)
@Data
public class CfgViewSyncRecordPo extends BasePoEntity implements Serializable {

    /**
     * 源视图Bid
     */
    private String sourceBid;

    /**
     * 目标视图Bid集合
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> targetBids;

}