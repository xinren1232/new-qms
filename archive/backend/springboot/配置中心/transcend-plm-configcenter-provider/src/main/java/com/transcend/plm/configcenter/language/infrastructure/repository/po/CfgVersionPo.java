package com.transcend.plm.configcenter.language.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 属性表
 * @TableName cfg_attribute
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Data
@TableName("cfg_version")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CfgVersionPo extends BasePoEntity implements Serializable {

    /**
     * 名称
     */
    private String name;

    private Integer version;


    private static final long serialVersionUID = 1L;

}