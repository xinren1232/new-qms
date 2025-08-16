package com.transcend.plm.datadriven.infrastructure.draft.po;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class DraftPO implements Serializable {

    private Long id;

    private String bid ;

    private String dataBid;

    private String baseModel;

    private String content;

    /**
     * 未启用0，启用1，禁用2
     */
    private Short enableFlag;

    private Long tenantId;

    /**
     * 是否删除（0-未删除；1已删除）
     */
    private Integer deleteFlag=0;

    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;

}
