package com.transcend.plm.configcenter.draft.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 草稿表
 * @TableName cfg_draft
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Data
@TableName("cfg_draft")
public class CfgDraftPo extends BasePoEntity implements Serializable {

    /**
     * 业务code
     */
    private String bizCode;

    /**
     * 类别
     */
    private String category;

    /**
     * 内容
     */
    private String content;


    private static final long serialVersionUID = 1L;

}