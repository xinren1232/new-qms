package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectPathChainQO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transsion.framework.annotation.PermissionLimit;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgObjectRelationFeignClient {

    /**
     * 获取关系转换规则
     * @param objectRelationRuleQo 视图规则匹配入参
     * @return
     */
    @PostMapping("/api/cfg/object-relation/getRelationRuleRes")
    @PermissionLimit(limit = false)
    TranscendApiResponse<String> getRelationRuleRes(@RequestBody ObjectRelationRuleQo objectRelationRuleQo);

    /**
     * 查看关系
     */
    @GetMapping("/api/cfg/object-relation/getRelation/{modelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgObjectRelationVo> getRelation(@PathVariable("modelCode") String modelCode);


    /**
     * 以目标对象查看关系列表
     */
    @GetMapping("/api/cfg/object-relation/listRelationByTargetModelCode/{modelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<List<CfgObjectRelationVo>> listRelationByTargetModelCode(@PathVariable("modelCode") String modelCode);

    /**
     * 以源和目标对象查看关系列表
     */
    @GetMapping("/api/cfg/object-relation/listRelationBySTModelCode/{sourceModelCode}/{targetModelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<List<CfgObjectRelationVo>> listRelationBySTModelCode(@PathVariable("sourceModelCode") String sourceModelCode,
                                                                              @PathVariable("targetModelCode") String targetModelCode);
    /**
     * 以源和目标对象查看跨层级关系列表
     * 如：源对象为A，目标对象为D，中间有B、C两个对象，A->B->C->D 那么查询的关系列表为A->B、A->C、A->D
     * 如果也存在 A->B->D 那么查询的关系列表为A->B、A->D
     * @param sourceModelCode 源对象
     * @param targetModelCode 目标对象
     */
    @GetMapping("/api/cfg/object-relation/listCrossRelationByModelCode/sourceModelCode/{sourceModelCode}/targetModelCode/{targetModelCode}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<Map<String, List<CfgObjectRelationVo>>> listGroupCrossRelationBySTModelCode(@PathVariable("sourceModelCode") String sourceModelCode,
                                                                                                     @PathVariable("targetModelCode") String targetModelCode,
                                                                                                     @RequestParam(value = "conditionModelCodes", required = false) Set<String> conditionModelCodes);

    @PostMapping("/api/cfg/object-relation/listCrossRelationByPathChain")
    @PermissionLimit(limit = false)
    TranscendApiResponse<Map<String, CfgObjectRelationVo>> listCrossRelationByPathChain(@RequestBody List<ObjectPathChainQO> qos);


    @ApiOperation(value = "获取关系列表", httpMethod = "GET")
    @GetMapping("/api/cfg/object-relation/listRelationTab/{modelCode}")
    TranscendApiResponse<List<CfgObjectRelationVo>> listRelationTab(@PathVariable(name = "modelCode") String modelCode);
}
