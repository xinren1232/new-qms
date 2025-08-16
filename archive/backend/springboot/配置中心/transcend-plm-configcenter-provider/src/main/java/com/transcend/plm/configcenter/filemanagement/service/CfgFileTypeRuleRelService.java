package com.transcend.plm.configcenter.filemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileTypeRuleRel;

import java.util.List;

/**
 *
 */
public interface CfgFileTypeRuleRelService extends IService<CfgFileTypeRuleRel> {

    List<CfgFileTypeRelRuleNameVo> queryFileTypeCodeWithRuleNameList(String copyRuleName);
}
