package com.transcend.plm.configcenter.draft.pojo;

import com.transcend.plm.configcenter.draft.infrastructure.repository.po.CfgDraftPo;
import com.transcend.plm.configcenter.draft.pojo.dto.CfgDraftDto;
import com.transcend.plm.configcenter.draft.pojo.qo.CfgDraftQo;
import com.transcend.plm.configcenter.draft.pojo.vo.CfgDraftVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgDraftConverter {

    CfgDraftConverter INSTANCE = Mappers.getMapper(CfgDraftConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgDraftPo dto2po(CfgDraftDto dto);

    /**
     * do 转化为 vo
     * @param cfgAttribute
     * @return
     */
    CfgDraftVo po2vo(CfgDraftPo cfgAttribute);

    /**
     * qo 转化为 po
     * @param cfgAttributeQo
     * @return
     */
    CfgDraftPo qo2po(CfgDraftQo cfgAttributeQo);

    /**
     * do 转化为 vo
     * @param cfgAttributes
     * @return
     */
    List<CfgDraftVo> pos2vos(List<CfgDraftPo> cfgAttributes);

    CfgDraftDto vo2dto(CfgDraftVo vo);
}
