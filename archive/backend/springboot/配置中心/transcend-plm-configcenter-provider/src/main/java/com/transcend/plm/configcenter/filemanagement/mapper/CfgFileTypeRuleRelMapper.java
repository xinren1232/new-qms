package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileTypeRuleRel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.CfgFileTypeRuleRel
 */
public interface CfgFileTypeRuleRelMapper extends BaseMapper<CfgFileTypeRuleRel> {

    int deleteByRuleBid(@Param("ruleBid") String ruleBid,@Param("fileTypeBidList") List<String> fileTypeBidList);

    int deleteByFileTypeBid(@Param("fileTypeBid") String fileTypeBid,@Param("ruleBidList") List<String> ruleBidList);

    List<CfgFileTypeRelRuleNameVo> queryFileTypeCodeWithRuleNameList(@Param("copyRuleName") String copyRuleName);

}




