package com.transcend.plm.configcenter.object.application.service.impl;

import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectLifecycleRoot;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectRoot;
import com.transcend.plm.configcenter.object.domain.entity.CfgObjectLifecycle;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleEditParam;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transsion.framework.common.ObjectUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ObjectLifecycleAppServiceImpl
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:28
 */
@Service
public class CfgObjectLifecycleAppServiceImpl implements ICfgObjectLifecycleAppService {

    @Override
    public ObjectModelLifeCycleVO findObjectLifecycleByModelCode(String modelCode) {
        return CfgObjectLifecycle.buildWithFind(modelCode).findAndPopulate();
    }

    @Override
    public Boolean saveObjectLifeCycle(ObjectLifeCycleSaveParam param) {
        return CfgObjectLifecycle.buildWithSave(param).save();
    }

    @Override
    @CacheEvict(value = "OBJECT_LIFE_CYCLE", allEntries = true)
    public Boolean updateObjectLifeCycle(ObjectLifeCycleEditParam param) {
        return CfgObjectLifecycle.buildWithEdit(param).update();
    }

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */

    @Override
    public Map<String, ObjectModelLifeCycleVO> listLifecycleByModelCodes(List<String> modelCodeList) {
        return CfgObjectLifecycleRoot.build().findLifecycleByModelCodeList(modelCodeList);
    }

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */

    @Override
    public ObjectModelLifeCycleVO findObjectLifecycleByObjectBid(String objectBid) {
        String modelCode = CfgObject.buildWithBid(objectBid).toModelCode();
        ObjectModelLifeCycleVO vo = CfgObjectLifecycle.buildWithFind(modelCode).findAndPopulate();
        if (ObjectUtil.isEmpty(vo)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                    "获取不到对象bid为【" + objectBid + "】的生命周期");
        }
        return vo;
    }

    @Override
    public List<ObjectModelLifeCycleVO> findChildrenLifecycleByObjectBid(String objectBid) {
        String modelCode = CfgObject.buildWithBid(objectBid).toModelCode();
        List<String> modelCodeList = CfgObjectRoot.build().findChildrenListByModelCode(modelCode).conversionToModelCode();
        return CfgObjectLifecycleRoot.build().findLifecycleInModelCodeList(modelCodeList);
    }

    @Override
    public Map<String, ObjectModelLifeCycleVO> findLifecycleListByObjectBidList(List<String> objectBidList) {
        return CfgObjectLifecycleRoot.build().findLifecycleByObjectBidList(objectBidList);
    }

}
