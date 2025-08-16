package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;

import java.util.Collection;
import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_role_access】的数据库操作Service
* @createDate 2023-09-20 16:15:29
*/
public interface ApmRoleAccessService extends IService<ApmRoleAccess> {

    /**
     * 通过roleBid和sphereBid查询角色权限
     * 方法描述
     * @param roleBidList roleBidList
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRoleAccess> getRoleAccessByRoleBidAndSphereBid(List<String> roleBidList, String sphereBid);

    /**
     * 通过roleBidList查询对象权限
     * @param roleBidList 角色bid列表
     * @return 对象权限列表
     */
    List<ApmSpaceAppAccessVo> getAccessByRoleBidList(List<String> roleBidList);

    /**
     *
     * 方法描述
     * @param spaceAppBids spaceAppBids
     * @return 返回值
     */
    List<ApmRoleAccess> listBySpaceAppBids(Collection spaceAppBids);
}
