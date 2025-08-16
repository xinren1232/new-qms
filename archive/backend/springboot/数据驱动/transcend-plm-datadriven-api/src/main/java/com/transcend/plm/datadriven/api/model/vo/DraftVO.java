package com.transcend.plm.datadriven.api.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;

/**
 * 从com.transcend.plm.datadriven.infrastructure.draft.po.DraftPo复制过来
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class DraftVO{
    private Long id;
    private String bid ;
    private String modelCode;
    private String dataBid;
    private String baseModel;
    private String content;
    private Short enableFlag;
    private Long tenantId;
    private Integer deleteFlag=0;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;
}
