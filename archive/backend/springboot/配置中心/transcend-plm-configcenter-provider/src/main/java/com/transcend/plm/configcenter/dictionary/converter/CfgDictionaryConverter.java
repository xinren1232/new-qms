package com.transcend.plm.configcenter.dictionary.converter;

import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-25 11:06
 **/
@Mapper
public interface CfgDictionaryConverter {

    CfgDictionaryConverter INSTANCE = Mappers.getMapper(CfgDictionaryConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgDictionaryPo dto2po(CfgDictionaryDto dto);

    List<CfgDictionaryPo> dto2po(List<CfgDictionaryDto> dtos);

    /**
     * do 转化为 vo
     * @param cfgAttribute
     * @return
     */
    CfgDictionaryVo po2vo(CfgDictionaryPo cfgAttribute);

    List<CfgDictionaryVo> po2vo(List<CfgDictionaryPo> cfgAttributes);

    /**
     * qo 转化为 po
     * @param CfgdictionaryQo
     * @return
     */
    CfgDictionaryPo qo2po(CfgDictionaryQo CfgdictionaryQo);

    /**
     * do 转化为 vo
     * @param cfgAttributes
     * @return
     */
    List<CfgDictionaryVo> pos2vos(List<CfgDictionaryPo> cfgAttributes);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgDictionaryPo> dtos2pos(List<CfgDictionaryDto> cfgAttributeDtos);

    CfgDictionaryDto vo2dto(CfgDictionaryVo cfgDictionaryVo);
}
