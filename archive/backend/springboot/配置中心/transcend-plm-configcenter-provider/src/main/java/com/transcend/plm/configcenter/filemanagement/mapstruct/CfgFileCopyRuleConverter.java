package com.transcend.plm.configcenter.filemanagement.mapstruct;

import com.transcend.plm.configcenter.filemanagement.domain.CfgFileCopyRule;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.CfgFileCopyRuleDto;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileCopyRuleVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CfgFileCopyRuleConverter {
    CfgFileCopyRuleConverter INSTANCE = Mappers.getMapper(CfgFileCopyRuleConverter.class);


    CfgFileCopyRule dto2po(CfgFileCopyRuleDto dto);

    List<CfgFileCopyRuleVo> pos2vos(List<CfgFileCopyRule> pos);

    CfgFileCopyRuleVo po2vo(CfgFileCopyRule po);

}
