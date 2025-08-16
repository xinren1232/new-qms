package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author bin.yin
 * @description: 文件类型code 和 复制规则名称
 * @version:
 * @date 2024/04/30 10:23
 */
@Data
public class CfgFileTypeRelRuleNameVo implements Serializable {
    /**
     * 文件类型code
     */
    private String fileTypeCode;
    /**
     * 复制规则名称
     */
    private String copyRuleName;

    private static final long serialVersionUID = 1L;
}
