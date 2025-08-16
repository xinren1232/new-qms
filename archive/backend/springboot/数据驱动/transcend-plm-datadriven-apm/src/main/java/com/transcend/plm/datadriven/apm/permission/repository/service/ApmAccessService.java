package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_access】的数据库操作Service
* @createDate 2023-09-20 16:15:29
*/
public interface ApmAccessService extends IService<ApmAccess> {

    /**
     * 返回根据角色和资源领域查询的ApmAccess列表。
     *
     * @param roleBids  角色的BID集合
     * @param sphereBid 资源领域的BID
     * @return 根据角色和资源领域查询的ApmAccess列表
     */
    List<ApmAccess> listByRoles(Set<String> roleBids, String sphereBid);

    /**
     * 返回根据资源领域的BID查询带有角色的ApmActionConfigVo列表。
     *
     * @param sphereBid 资源领域的BID
     * @return 带有角色的ApmActionConfigVo列表
     */
    List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String sphereBid);


    /**
     * 更新ApmAccess对象的数据库记录。
     *
     * @param apmAccess 要更新的ApmAccess对象
     * @return 更新成功返回true，否则返回false
     */
    boolean updateByBid(ApmAccess apmAccess);

    /**
     * 返回根据订单BID集合查询的ApmAccess列表。
     *
     * @param bids 订单BID集合
     * @return 根据订单BID集合查询的ApmAccess列表
     */
    List<ApmAccess> listByBids(Set<String> bids);

    /**
     * 根据订单BID集合查询ApmAccess列表。
     *
     * @param bids 订单BID集合
     * @return 根据订单BID集合查询的ApmAccess列表
     */
    List<ApmAccess> listByBidConllection(Collection bids);
}
