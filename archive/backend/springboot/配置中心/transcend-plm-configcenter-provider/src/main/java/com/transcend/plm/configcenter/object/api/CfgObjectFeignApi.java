package com.transcend.plm.configcenter.object.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transcend.plm.configcenter.object.application.service.ICfgRelationObjectConfigAppService;
import io.swagger.annotations.Api;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 14:24
 * @since 1.0
 */
@Api(value = "CfgObject Controller", tags = "API-对象-控制器")
@Validated
@RestController
public class CfgObjectFeignApi implements CfgObjectFeignClient {

    @Resource
    private ICfgObjectAppService objectModelAppService;

    @Resource
    private ICfgObjectLifecycleAppService lifecycleAppService;

    @Resource
    private ICfgRelationObjectConfigAppService cfgRelationObjectConfigAppService;

    @Override
    public TranscendApiResponse<List<ObjectRelationVO>> getObjectRelationVOsBySourceModelCode(String sourceModelCode) {
        return TranscendApiResponse.success(
                cfgRelationObjectConfigAppService.getObjectRelationVOsBySourceModelCode(sourceModelCode)
        );
    }

    @Override
//    @Cacheable(value = CacheNameConstant.OBJECT_MODEL, key = "#modelCode")
    public TranscendApiResponse<CfgObjectVo> getByModelCode(String modelCode) {
        return TranscendApiResponse.success(
                objectModelAppService.getObjectAndAttributeByModelCode(modelCode)
        );
    }

    @Override
    public TranscendApiResponse<ObjectModelLifeCycleVO> findObjectLifecycleByModelCode(@PathVariable(name = "modelCode") String modelCode) {
        return TranscendApiResponse.success(
                lifecycleAppService.findObjectLifecycleByModelCode(modelCode)
        );
    }

    @Override
    public TranscendApiResponse<CfgObjectVo> getByBaseModel(String baseModel) {
//        return objectModelAppService.getObjectAndAttributeByBaseModel(baseModel);
        return null;
    }

    @Override
    public TranscendApiResponse<List<CfgObjectVo>> listAllByModelCode(@PathVariable String modelCode) {
        return TranscendApiResponse.success(objectModelAppService.listAllByModelCode(modelCode));
    }

    @Override
    public TranscendApiResponse<List<CfgObjectVo>> listByModelCodes(List<String> modelCodes) {
        return TranscendApiResponse.success(objectModelAppService.listByModelCodes(modelCodes));
    }

    @Override
    public TranscendApiResponse<List<CfgObjectVo>> listChildrenByModelCode(String modelCode) {
        return TranscendApiResponse.success(objectModelAppService.listChildrenByModelCode(modelCode));
    }

    @Override
    public TranscendApiResponse<List<CfgObjectVo>> list() {
        return TranscendApiResponse.success(objectModelAppService.list());
    }

    @Override
    public TranscendApiResponse<List<CfgObjectAttributeVo>> listObjectAttribute(String modelCode) {
        return TranscendApiResponse.success(objectModelAppService.listAttrsByModelCode(modelCode));
    }
}
