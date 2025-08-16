package com.transcend.plm.configcenter.api.model.mothed.dto;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import lombok.Data;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-07 15:06
 **/
@Data
public class CfgEventDto extends BaseDto {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String description;
}
