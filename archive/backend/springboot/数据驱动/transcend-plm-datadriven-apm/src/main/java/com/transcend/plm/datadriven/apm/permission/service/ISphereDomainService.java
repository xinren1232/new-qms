package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereCopyAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmSphereVO;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @description 空间领域服务
 * @createTime 2023-09-25 15:52:00
 */
public interface ISphereDomainService {
    /**
     * 新增空间领域
     *
     * @param apmSphereAO 空间领域AO
     * @return 空间领域VO
     */
    ApmSphere add(ApmSphereAO apmSphereAO);

    /**
     * 删除空间领域
     *
     * @param apmSphereAO 空间领域AO
     * @return 空间领域VO
     */
    Boolean delete(ApmSphereAO apmSphereAO);

    /**
     * 空间应用域拷贝
     *
     * @param apmSphereCopyAO 空间领域拷贝
     * @return List<Map < String, String>>
     */
    List<Map<String, String>> copySphere(ApmSphereCopyAO apmSphereCopyAO);

    /**
     * 空间应用域拷贝
     *
     * @param spaceAppBid spaceAppBid
     * @param spaceBid    spaceBid
     * @param nowAppBid   nowAppBid
     * @return List<Map < String, String>>
     */

    List<Map<String, String>> copySpaceAppSphere(String spaceAppBid, String spaceBid, String nowAppBid);

    /**
     * 修改空间领域
     *
     * @param apmSphereAO 空间领域AO
     * @return ApmSphereVO
     */
    ApmSphereVO update(ApmSphereAO apmSphereAO);

    /**
     * 查询空间领域
     *
     * @param apmSphereAO 空间领域AO
     * @return ApmSphereVO
     */
    ApmSphereVO query(ApmSphereAO apmSphereAO);
}
