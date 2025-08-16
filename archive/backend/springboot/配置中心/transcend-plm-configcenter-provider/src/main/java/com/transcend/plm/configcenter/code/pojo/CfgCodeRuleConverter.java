package com.transcend.plm.configcenter.code.pojo;

import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRulePo;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRulePoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
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
public interface CfgCodeRuleConverter {

    CfgCodeRuleConverter INSTANCE = Mappers.getMapper(CfgCodeRuleConverter.class);

    /**
     * dto2po
     * @param dto
     * @return
     */
    CfgCodeRulePo dto2po(CfgCodeRulePoDto dto);

    /**
     * po2vo
     * @param po
     * @return
     */
    CfgCodeRulePoVo po2vo(CfgCodeRulePo po);

    /**
     * qo2po
     * @param param
     * @return
     */
    CfgCodeRulePo qo2po(CfgCodeRulePoQo param);

    /**
     * pos2vos
     * @param records
     * @return
     */
    List<CfgCodeRulePoVo> pos2vos(List<CfgCodeRulePo> records);

    /**
     * dtos2pos
     * @param cfgCodeRuleDtos
     * @return
     */
    List<CfgCodeRulePo> dtos2pos(List<CfgCodeRulePoDto> cfgCodeRuleDtos);
}
