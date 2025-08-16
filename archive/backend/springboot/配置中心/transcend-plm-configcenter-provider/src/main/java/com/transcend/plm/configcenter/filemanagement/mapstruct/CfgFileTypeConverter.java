package com.transcend.plm.configcenter.filemanagement.mapstruct;

import com.transcend.plm.configcenter.filemanagement.domain.CfgFileType;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.CfgFileTypeDto;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CfgFileTypeConverter {
    CfgFileTypeConverter INSTANCE = Mappers.getMapper(CfgFileTypeConverter.class);
    CfgFileType dto2po(CfgFileTypeDto dto);

    List<CfgFileTypeVo> pos2vos(List<CfgFileType> pos);

    CfgFileTypeVo po2vo(CfgFileType po);
}
