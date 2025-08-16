package com.transcend.plm.datadriven.apm.permission.service;


import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmAccessVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigUserVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;

import java.util.List;

/**
 * @author unknown
 */
public interface IApmAccessDomainService {
    /**
     * 创建
     *
     * @param apmAccessAO apmAccessAO
     * @return ApmAccessVO
     */
    ApmAccessVO save(ApmAccessAO apmAccessAO);

    /**
     * 修改
     *
     * @param apmAccessAO
     * @return ApmAccessVO
     */
    ApmAccessVO update(ApmAccessAO apmAccessAO);

    /**
     * 逻辑删除
     *
     * @param bid bid
     * @return boolean
     */
    boolean logicDelete(String bid);

    /**
     * 查询列表
     *
     * @return {@link List<ApmAccessVO>}
     */
    List<ApmAccessVO> list();

    /**
     * queryAccessWithRoleBySphereBid
     *
     * @param sphereBid sphereBid
     * @return {@link List<ApmActionConfigVo>}
     */
    List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String sphereBid);

    /**
     * currentUserAccess
     *
     * @param apmSphere apmSphere
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> currentUserAccess(ApmSphere apmSphere);
}
