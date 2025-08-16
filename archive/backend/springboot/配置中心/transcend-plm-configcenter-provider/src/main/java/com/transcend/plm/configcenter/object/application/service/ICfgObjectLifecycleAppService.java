package com.transcend.plm.configcenter.object.application.service;

import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleEditParam;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;

import java.util.List;
import java.util.Map;

/**
 * ILifecycleAppService
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:27
 */
public interface ICfgObjectLifecycleAppService {

    ObjectModelLifeCycleVO findObjectLifecycleByModelCode(String modelCode);

    Boolean saveObjectLifeCycle(ObjectLifeCycleSaveParam param);

    Boolean updateObjectLifeCycle(ObjectLifeCycleEditParam param);

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */

    Map<String, ObjectModelLifeCycleVO> listLifecycleByModelCodes(List<String> modelCodeList);

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */

    ObjectModelLifeCycleVO findObjectLifecycleByObjectBid(String objectBid);

    List<ObjectModelLifeCycleVO> findChildrenLifecycleByObjectBid(String objectBid);

    Map<String, ObjectModelLifeCycleVO> findLifecycleListByObjectBidList(List<String> objectBidList);

}
