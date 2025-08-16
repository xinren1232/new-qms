package com.transcend.plm.configcenter.object.application.service;

import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectViewVO;

import java.util.List;

/**
 * IObjectViewAppService
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:27
 */
public interface ICfgObjectViewAppService {

    List<ObjectViewVO> listByModelCode(String modelCode);

    Boolean editObjectView(CfgObjectViewRuleEditParam param);

    List<Boolean> saveOrUpdate(CfgObjectViewRuleEditParam cfgObjectViewRuleEditParam);

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */



    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */


}
