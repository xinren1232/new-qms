package com.transcend.plm.configcenter.language.pojo;

import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgLanguagePo;
import com.transcend.plm.configcenter.language.pojo.dto.CfgLanguageDto;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
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
public interface CfgLanguageConverter {

    CfgLanguageConverter INSTANCE = Mappers.getMapper(CfgLanguageConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgLanguagePo dto2po(CfgLanguageDto dto);

    /**
     * do 转化为 vo
     * @param cfgLanguagePo
     * @return
     */
    CfgLanguageVo po2vo(CfgLanguagePo cfgLanguagePo);

    /**
     * qo 转化为 po
     * @param cfgLanguageQo
     * @return
     */
    CfgLanguagePo qo2po(CfgLanguageQo cfgLanguageQo);

    /**
     * do 转化为 vo
     * @param cfgLanguagePos
     * @return
     */
    List<CfgLanguageVo> pos2vos(List<CfgLanguagePo> cfgLanguagePos);

    /**
     * dto 批量转化为 po
     * @param cfgLanguageDtos
     * @return
     */
    List<CfgLanguagePo> dtos2pos(List<CfgLanguageDto> cfgLanguageDtos);
}
