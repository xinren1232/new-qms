package com.transcend.plm.configcenter.objectrelation.domain.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectPathChainQO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface IObjectRelationService {
    boolean add(CfgObjectRelationDto cfgObjectRelationDto);

    PagedResult<CfgObjectRelationVo> page(BaseRequest<CfgObjectRelationQo> pageQo);

    boolean delete(String bid);

    boolean edit(CfgObjectRelationDto cfgObjectRelationDto);

    boolean changeEnableFlag(String bid, Integer enableFlag);

    String getRelationRuleRes(ObjectRelationRuleQo objectRelationRuleQo);

    List<CfgObjectRelationVo> listRelationTab(String modelCode);

    CfgObjectRelationVo getRelationByModelCode(String modelCode);

    List<CfgObjectRelationVo> listRelationByTargetModelCode(String modelCode);

    List<CfgObjectRelationVo> listRelationBySTModelCode(String sourceModelCode,String targetModelCode);

    /**
     * 以源和目标对象查看跨层级关系列表
     * 如：源对象为A，目标对象为D，中间有B、C两个对象，A->B->C->D 那么查询的关系列表为A->B、A->C、A->D
     * 如果也存在 A->B->D 那么查询的关系列表为A->B、A->D
     * @param sourceModelCode 源对象
     * @param targetModelCode 目标对象
     */
    Map<String, List<CfgObjectRelationVo>> listGroupCrossRelationBySTModelCode(String sourceModelCode, String targetModelCode, Set<String> conditionModelCodes);

    Map<String, CfgObjectRelationVo> listCrossRelationByPathChain(List<ObjectPathChainQO> qos);
}
