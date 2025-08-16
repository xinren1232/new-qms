package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import com.transcend.plm.configcenter.api.model.object.dto.CfgModelEventMethodDto;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:18
 **/
@Mapper
public interface CfgModelEventMethodConverter {

    CfgModelEventMethodConverter INSTANCE = Mappers.getMapper(CfgModelEventMethodConverter.class);

    /**
     * dto2po
     * @param dto
     * @return
     */
    CfgModelEventMethodPo dto2po(CfgModelEventMethodDto dto);

    /**
     * po2vo
     * @param po
     * @return
     */
    CfgModelEventMethodVo po2vo(CfgModelEventMethodPo po);

    /**
     * qo2po
     * @param param
     * @return
     */
    CfgModelEventMethodPo qo2po(CfgModelEventMethodQo param);

    /**
     * pos2vos
     * @param records
     * @return
     */
    List<CfgModelEventMethodVo> pos2vos(List<CfgModelEventMethodPo> records);

    /**
     * dtos2pos
     * @param dtos
     * @return
     */
    List<CfgModelEventMethodPo> dtos2pos(List<CfgModelEventMethodDto> dtos);
}
