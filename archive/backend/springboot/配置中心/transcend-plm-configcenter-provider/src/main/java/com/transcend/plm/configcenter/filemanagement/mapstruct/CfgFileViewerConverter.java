package com.transcend.plm.configcenter.filemanagement.mapstruct;

import com.transcend.plm.configcenter.filemanagement.domain.CfgFileViewer;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.CfgFileViewerDto;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileViewerVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CfgFileViewerConverter {
    CfgFileViewerConverter INSTANCE = Mappers.getMapper(CfgFileViewerConverter.class);


    CfgFileViewer dto2po(CfgFileViewerDto dto);

    List<CfgFileViewerVo> pos2vos(List<CfgFileViewer> pos);

    CfgFileViewerVo po2vo(CfgFileViewer po);
}
