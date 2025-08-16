package com.transcend.plm.configcenter.filemanagement.mapstruct;

import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibrary;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.CfgFileLibraryDto;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileLibraryVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CfgFileLibraryConverter {
    CfgFileLibraryConverter INSTANCE = Mappers.getMapper(CfgFileLibraryConverter.class);

    CfgFileLibrary dot2po(CfgFileLibraryDto dto);

    List<CfgFileLibraryVo> pos2vos(List<CfgFileLibrary> pos);

    CfgFileLibraryVo po2vo(CfgFileLibrary po);
}
