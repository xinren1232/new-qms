package com.transcend.plm.configcenter.objectrelation.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectPathChainQO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.objectrelation.domain.service.IObjectRelationService;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API-对象关系-控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 14:24
 * @since 1.0
 */
@Api(value = "CfgObjectRelation Controller", tags = "API-对象关系-控制器")
@Validated
@RestController
public class CfgObjectRelationFeignApi implements CfgObjectRelationFeignClient {
    @Resource
    private IObjectRelationService objectRelationService;

    @Override
    public TranscendApiResponse<String> getRelationRuleRes(ObjectRelationRuleQo objectRelationRuleQo) {
        return TranscendApiResponse.success(objectRelationService.getRelationRuleRes(objectRelationRuleQo));
    }

    @Override
    public TranscendApiResponse<CfgObjectRelationVo> getRelation(String modelCode) {
        return TranscendApiResponse.success(objectRelationService.getRelationByModelCode(modelCode));
    }

    /**
     * 根据模型目标对象编码查询关系表
     *
     * @param modelCode 目标modelCode
     * @return
     */
    @Override
    public TranscendApiResponse<List<CfgObjectRelationVo>> listRelationByTargetModelCode(String modelCode) {
        return TranscendApiResponse.success(objectRelationService.listRelationByTargetModelCode(modelCode));
    }

    @Override
    public TranscendApiResponse<List<CfgObjectRelationVo>> listRelationBySTModelCode(String sourceModelCode, String targetModelCode) {
        return TranscendApiResponse.success(objectRelationService.listRelationBySTModelCode(sourceModelCode, targetModelCode));
    }

    /**
     * 以源和目标对象查看跨层级关系列表
     * 如：源对象为A，目标对象为D，中间有B、C两个对象，A->B->C->D 那么查询的关系列表为A->B、A->C、A->D
     * 如果也存在 A->B->D 那么查询的关系列表为A->B、A->D
     *
     * @param sourceModelCode 源对象
     * @param targetModelCode 目标对象
     */
    @Override
    public TranscendApiResponse<Map<String, List<CfgObjectRelationVo>>> listGroupCrossRelationBySTModelCode(String sourceModelCode,
                                                                                                            String targetModelCode,
                                                                                                            Set<String> conditionModelCodes) {
        return TranscendApiResponse.success(objectRelationService.listGroupCrossRelationBySTModelCode(sourceModelCode, targetModelCode, conditionModelCodes));
    }

    @Override
    public TranscendApiResponse<Map<String, CfgObjectRelationVo>> listCrossRelationByPathChain(List<ObjectPathChainQO> qos) {
        return TranscendApiResponse.success(objectRelationService.listCrossRelationByPathChain(qos));
    }

    @Override
//    @Cacheable(value = CacheNameConstant.OBJECT_RELATION_TAB_LIST, key = "#modelCode")
    public TranscendApiResponse<List<CfgObjectRelationVo>> listRelationTab(@PathVariable(name = "modelCode") String modelCode) {
        return TranscendApiResponse.success(objectRelationService.listRelationTab(modelCode));
    }
}
