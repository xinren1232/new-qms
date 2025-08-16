package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmIdentity;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色领域服务接口
 * @createTime 2023-09-20 15:01:00
 */
public interface IApmRoleDomainService {
    /**
     * 添加角色
     *
     * @param apmRoleAO apmRoleAO
     * @return {@link ApmRoleVO}
     */
    ApmRoleVO add(ApmRoleAO apmRoleAO);

    /**
     * 查询角色
     *
     * @param apmRoleAO apmRoleAO
     * @return {@link ApmRoleVO}
     */
    ApmRoleVO update(ApmRoleAO apmRoleAO);

    /**
     * 查询角色
     *
     * @param bid bid
     * @return {@link ApmRoleVO}
     */
    ApmRoleVO getByBid(String bid);

    /**
     * 删除角色
     *
     * @param bid bid
     * @return {@link Boolean}
     */
    Boolean deleteByBid(String bid);

    /**
     * list
     *
     * @param apmRoleQO apmRoleQO
     * @return {@link List<ApmRoleVO>}
     */
    List<ApmRoleVO> list(ApmRoleQO apmRoleQO);

    /**
     * getIdentityByRole
     *
     * @param roleBid roleBid
     * @return {@link ApmIdentity}
     */
    ApmIdentity getIdentityByRole(String roleBid);

    /**
     * getRoleAndIdentityByRoleBids
     *
     * @param roleBids roleBids
     * @return {@link List<ApmRoleAndIdentityVo>}
     */
    List<ApmRoleAndIdentityVo> getRoleAndIdentityByRoleBids(List<String> roleBids);

    /**
     * 通过当前登录人和sphereBid查询角色
     *
     * @param jobNum    jobNum
     * @param sphereBid sphereBid
     * @return {@link List<ApmRole>}
     */
    List<ApmRole> listRoleByJobNumAndSphereBid(String jobNum, String sphereBid);

    /**
     * addSphereAndDefaultRole
     */
    void addSphereAndDefaultRole();

    /**
     * 通过空间bid查询登录人是否为空间管理员角色
     *
     * @param spaceBid 空间bid
     * @return 是否为空间管理员角色
     */
    Boolean isSpaceAdmin(String spaceBid);
    /**
     * 通过空间bid查询登录人是否为空间管理员角色 + 全局管理员角色
     *
     * @param spaceBid 空间bid
     * @return 是否为空间管理员角色
     */
    Boolean isSpaceAdminOrGlobalAdmin(String spaceBid);

    /**
     * 查询当前登录人是否为全局管理员
     *
     * @return 是否为全局管理员
     */
    Boolean isGlobalAdmin();

    /**
     * remove
     *
     * @param apmRoleDto apmRoleDto
     * @return {@link boolean}
     */
    boolean remove(ApmRoleDto apmRoleDto);

    /**
     * 根据人缓存查询目标的角色集（包括空间，应用），另外需要把控角色人员配置时，需要删除改缓存  TODO LUOJIE
     *
     * @param jobNumber jobNumber
     * @param spareBid  spareBid
     * @return {@link List<String>}
     */
    List<String> listRolesByUser(String jobNumber, String spareBid);
}
