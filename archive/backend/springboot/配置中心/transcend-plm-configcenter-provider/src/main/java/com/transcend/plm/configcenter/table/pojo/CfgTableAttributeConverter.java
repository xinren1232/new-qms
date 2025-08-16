package com.transcend.plm.configcenter.table.pojo;

import com.transcend.plm.configcenter.table.domain.entity.CfgTable;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableAttributeDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-25 11:06
 **/
@Mapper
public interface CfgTableAttributeConverter {


    CfgTableAttributeConverter INSTANCE = Mappers.getMapper(CfgTableAttributeConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgTableAttributePo dto2po(CfgTableAttributeDto dto);


    /**
     * dto 转化为 do
     * @param CfgTableAttributePos
     * @return
     */
    CfgTableAttributeVo po2vo(CfgTableAttributePo CfgTableAttributePos);

    /**
     * do 转化为 vo
     * @param CfgTableAttributePos
     * @return
     */
    List<CfgTableAttributeVo> pos2vos(List<CfgTableAttributePo> CfgTableAttributePos);


    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgTableAttributePo> dtos2pos(List<CfgTableAttributeDto> cfgAttributeDtos);


    /**
     * 实体转换为VO
     * @param cfgTable
     * @return CfgTableVo
     */
    CfgTableVo entity2vo(CfgTable cfgTable);

    /**
     * VO转换为实体
     * @param tableVo
     * @return CfgTable
     */
    CfgTable vo2entity(CfgTableVo tableVo);

    /**
     * vos 转化为 dtos
     */
    List<CfgTableAttributeDto> vos2dtos(List<CfgTableAttributeVo> vos);
}
