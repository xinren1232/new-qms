package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.plm.datadriven.api.model.MObject;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对象模型-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class ObjectModelDomainService extends AbstractObjectDomainService<MObject> {

    /**
     * @param modelService
     */
    public ObjectModelDomainService(ModelService<MObject> modelService) {
        this.modelService = modelService;
    }

    /**
     * @param modelCode
     * @param mBaseDataList
     * @return {@link Boolean }
     */
    @Override
    public Boolean addHisBatch(String modelCode, List<MObject> mBaseDataList) {
        return modelService.addHisBatch(modelCode,mBaseDataList);
    }
}
