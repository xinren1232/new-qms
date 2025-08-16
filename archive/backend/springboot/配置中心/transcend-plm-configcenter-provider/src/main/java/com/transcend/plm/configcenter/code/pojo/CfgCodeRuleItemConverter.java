package com.transcend.plm.configcenter.code.pojo;

import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRuleItemPo;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRuleItemPoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
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
public interface CfgCodeRuleItemConverter {

    CfgCodeRuleItemConverter INSTANCE = Mappers.getMapper(CfgCodeRuleItemConverter.class);

    /**
     * dto2po
     * @param dto
     * @return
     */
    CfgCodeRuleItemPo dto2po(CfgCodeRuleItemPoDto dto);

    /**
     * po2vo
     * @param po
     * @return
     */
    CfgCodeRuleItemPoVo po2vo(CfgCodeRuleItemPo po);

    /**
     * qo2po
     * @param qo
     * @return
     */
    CfgCodeRuleItemPo qo2po(CfgCodeRuleItemPoQo qo);

    /**
     * pos2vos
     * @param pos
     * @return
     */
    List<CfgCodeRuleItemPoVo> pos2vos(List<CfgCodeRuleItemPo> pos);

    /**
     * dtos2pos
     * @param dtos
     * @return
     */
    List<CfgCodeRuleItemPo> dtos2pos(List<CfgCodeRuleItemPoDto> dtos);
}
