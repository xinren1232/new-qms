package com.transcend.plm.configcenter.method.infrastructure.repository.po;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-07 14:59
 **/
@Data
public class CfgEvent extends BasePoEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String description;
}
