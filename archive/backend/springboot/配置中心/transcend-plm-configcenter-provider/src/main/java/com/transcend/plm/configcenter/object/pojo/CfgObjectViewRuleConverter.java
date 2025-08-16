package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectViewRuleQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
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
public interface CfgObjectViewRuleConverter {


    CfgObjectViewRuleConverter INSTANCE = Mappers.getMapper(CfgObjectViewRuleConverter.class);

    /**
     * po 转化为 vo
     * @param po
     * @return
     */
    CfgObjectViewRuleVo po2vo(CfgObjectViewRulePo po);

    /**
     * pos 转化为 vos
     * @param po
     * @return
     */
    List<CfgObjectViewRuleVo> pos2vos(List<CfgObjectViewRulePo> po);
    /**
     * vo 转化为 po
     * @param vo
     * @return
     */
    CfgObjectViewRulePo vo2po(CfgObjectViewRuleVo vo);


    CfgObjectViewRulePo saveParam2po(CfgObjectViewRuleEditParam saveParam);

    CfgObjectViewRulePo qo2po(CfgObjectViewRuleQo param);
}
