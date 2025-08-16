package com.transcend.plm.configcenter.attribute.pojo;

import com.transcend.plm.configcenter.attribute.infrastructure.repository.po.CfgAttributePo;
import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgAttributeConverter {

    CfgAttributeConverter INSTANCE = Mappers.getMapper(CfgAttributeConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgAttributePo dto2po(CfgAttributeDto dto);

    /**
     * do 转化为 vo
     * @param cfgAttributePo
     * @return
     */
    CfgAttributeVo po2vo(CfgAttributePo cfgAttributePo);

    /**
     * qo 转化为 po
     * @param cfgAttributeQo
     * @return
     */
    CfgAttributePo qo2po(CfgAttributeQo cfgAttributeQo);

    /**
     * do 转化为 vo
     * @param cfgAttributePos
     * @return
     */
    List<CfgAttributeVo> pos2vos(List<CfgAttributePo> cfgAttributePos);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgAttributePo> dtos2pos(List<CfgAttributeDto> cfgAttributeDtos);

    /**
     * do 转化为 vo
     * @param cfgAttributePo
     * @return
     */
    com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo po2ApiVo(CfgAttributePo cfgAttributePo);
}
