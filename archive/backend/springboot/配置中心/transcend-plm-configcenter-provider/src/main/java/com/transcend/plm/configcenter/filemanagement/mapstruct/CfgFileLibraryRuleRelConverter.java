package com.transcend.plm.configcenter.filemanagement.mapstruct;

import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibraryRuleRel;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileLibraryRuleRelVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CfgFileLibraryRuleRelConverter {
    CfgFileLibraryRuleRelConverter INSTANCE = Mappers.getMapper(CfgFileLibraryRuleRelConverter.class);

    CfgFileLibraryRuleRelVo po2vo(CfgFileLibraryRuleRel po);

}
