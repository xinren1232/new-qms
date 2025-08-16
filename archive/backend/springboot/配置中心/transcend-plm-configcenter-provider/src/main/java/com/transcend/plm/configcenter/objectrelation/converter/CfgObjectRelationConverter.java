package com.transcend.plm.configcenter.objectrelation.converter;

import java.util.List;

import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationPo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgObjectRelationConverter {

    CfgObjectRelationConverter INSTANCE = Mappers.getMapper(CfgObjectRelationConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgObjectRelationPo dto2po(CfgObjectRelationDto dto);

    /**
     * do 转化为 vo
     * @param cfgObjectRelationPo
     * @return
     */
    CfgObjectRelationVo po2vo(CfgObjectRelationPo cfgObjectRelationPo);

    /**
     * qo 转化为 po
     * @param cfgObjectRelationQo
     * @return
     */
    CfgObjectRelationPo qo2po(CfgObjectRelationQo cfgObjectRelationQo);

    /**
     * do 转化为 vo
     * @param cfgObjectRelationPos
     * @return
     */
    List<CfgObjectRelationVo> pos2vos(List<CfgObjectRelationPo> cfgObjectRelationPos);

    /**
     * dto 批量转化为 po
     * @param cfgObjectRelationDtos
     * @return
     */
    List<CfgObjectRelationPo> dtos2pos(List<CfgObjectRelationDto> cfgObjectRelationDtos);
        }
