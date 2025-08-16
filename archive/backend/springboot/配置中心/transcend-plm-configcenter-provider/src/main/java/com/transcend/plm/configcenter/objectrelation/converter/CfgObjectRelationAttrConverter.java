package com.transcend.plm.configcenter.objectrelation.converter;

import java.util.List;

import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationAttrDto;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationAttrVo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationAttrPo;
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
public interface CfgObjectRelationAttrConverter {

    CfgObjectRelationAttrConverter INSTANCE = Mappers.getMapper(CfgObjectRelationAttrConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgObjectRelationAttrPo dto2po(CfgObjectRelationAttrDto dto);

    /**
     * do 转化为 vo
     * @param cfgObjectRelationAttrPo
     * @return
     */
    CfgObjectRelationAttrVo po2vo(CfgObjectRelationAttrPo cfgObjectRelationAttrPo);


    /**
     * do 转化为 vo
     * @param cfgObjectRelationAttrPos
     * @return
     */
    List<CfgObjectRelationAttrVo> pos2vos(List<CfgObjectRelationAttrPo> cfgObjectRelationAttrPos);

    /**
     * dto 批量转化为 po
     * @param cfgObjectRelationAttrDtos
     * @return
     */
    List<CfgObjectRelationAttrPo> dtos2pos(List<CfgObjectRelationAttrDto> cfgObjectRelationAttrDtos);
        }
