package com.transcend.plm.configcenter.filemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileTypeRuleRel;
import com.transcend.plm.configcenter.filemanagement.mapper.CfgFileTypeRuleRelMapper;
import com.transcend.plm.configcenter.filemanagement.service.CfgFileTypeRuleRelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class CfgFileTypeRuleRelServiceImpl extends ServiceImpl<CfgFileTypeRuleRelMapper, CfgFileTypeRuleRel>
    implements CfgFileTypeRuleRelService {


    @Override
    public List<CfgFileTypeRelRuleNameVo> queryFileTypeCodeWithRuleNameList(String copyRuleName) {
        return baseMapper.queryFileTypeCodeWithRuleNameList(copyRuleName);
    }
}




