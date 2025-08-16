package com.transcend.plm.configcenter.language.pojo.dto;

import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgLanguagePo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@ApiModel("多语言")
public class CfgLanguageDto extends CfgLanguagePo implements Serializable {
    private static final long serialVersionUID = 1L;
}
