package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author peng.qin
 * @description 针对表【apm_role】的数据库操作Service
 * @createDate 2023-09-20 16:15:29
 */
public interface ApmRoleService extends IService<ApmRole> {

    /**
     * 根据角色编码查询角色信息
     *
     * @param code
     * @param sphereBid
     * @return
     */
    ApmRole getByCodeAndSphere(String code, String sphereBid);

    /**
     * 根据bid更新角色信息
     *
     * @param apmRole
     * @return
     */
    boolean updateByBid(ApmRole apmRole);

    /**
     * 方法描述
     *
     * @param bid bid
     * @return 返回值
     */
    ApmRole getByBid(String bid);

    /**
     * 方法描述
     *
     * @param bids bids
     * @return 返回值
     */
    List<ApmRole> listByBids(List<String> bids);

    /**
     * 方法描述
     *
     * @param bid bid
     * @return 返回值
     */
    Boolean removeByBid(String bid);

    /**
     * 方法描述
     *
     * @param apmRole apmRole
     * @return 返回值
     */
    List<ApmRole> listByCondition(ApmRole apmRole);

    /**
     * 方法描述
     *
     * @param bid bid
     * @return 返回值
     */
    List<ApmRole> listChildrenByBid(String bid);

    /**
     * 创建默认角色
     *
     * @param sphereBid 域bid
     * @param spaceName  spaceName
     * @return 是否创建成功
     */
    boolean createDefaultRole(String spaceName, String sphereBid);

    /**
     * 方法描述
     *
     * @param sphereBidMap sphereBidMap
     * @param notInCodes   notInCodes
     * @return 返回值
     */
    Map<String, String> copyRoles(Map<String, String> sphereBidMap, Set<String> notInCodes);

    /**
     * 根据域bid获取角色列表
     *
     * @param sphereBid 域bid
     * @return 角色列表
     */
    List<ApmRoleVO> getRoleListBySphereBid(String sphereBid);

    /**
     * 根据领域编码、身份列表获取角色列表。
     *
     * @param sphereBid    领域编码
     * @param identityList 身份列表
     * @return 角色列表
     */
    List<String> getRoleListBySphereBidAndIdentityList(String sphereBid, List<String> identityList);

    /**
     * 方法描述
     *
     * @param jobNum    jobNum
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRole> getRoleListByJobNumAndSphereBid(String jobNum, String sphereBid);

    /**
     * 方法描述
     *
     * @param roleBids    roleBids
     * @param spaceAppBid spaceAppBid
     * @return 返回值
     */
    List<ApmRoleVO> listByRoleBidsAndSpaceAppBid(List<String> roleBids, String spaceAppBid);

    /**
     * 方法描述
     *
     * @param roleBids roleBids
     * @return 返回值
     */
    List<ApmRoleVO> listByRoleBids(List<String> roleBids);

    /**
     * 方法描述
     *
     * @param codes     codes
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRoleVO> listByRoleBidsByCodes(List<String> codes, String sphereBid);

    /**
     * 方法描述
     * @param code    code
     * @param sphereBid sphereBid
     * @return 返回值
     */
    ApmRoleVO getByRoleBidsByCode(String code, String sphereBid);

    /**
     * 方法描述
     *
     * @param personResponsible personResponsible
     * @param spaceAppBid       spaceAppBid
     * @return 返回值
     */
    ApmRole getByCodeAndApp(String personResponsible, String spaceAppBid);

    /**
     * 根据外部唯一标识获取角色信息
     *
     * @param foreignBid
     * @return
     */
    ApmRoleVO getApmRoleVOByForeignBid(String foreignBid);

    /**
     * 新增角色
     *
     * @param apmRoleDto
     * @return
     */
    boolean createRole(ApmRoleDto apmRoleDto);

    /**
     * 方法描述
     *
     * @param apmRole apmRole
     * @return 返回值
     */
    boolean removeByCondition(ApmRole apmRole);

    /**
     * 方法描述
     *
     * @param apmRoleDto apmRoleDto
     * @return 返回值
     */
    boolean physicsRemove(ApmRoleDto apmRoleDto);

    /**
     * 方法描述
     *
     * @param jobNum     jobNum
     * @param sphereBids sphereBids
     * @return 返回值
     */
    List<ApmRole> getRoleListByJobNumAndSphereBidList(String jobNum, List<String> sphereBids);

    /**
     * 方法描述
     *
     * @param spaceBid     spaceBid
     * @param userNo userNo
     * @return 返回值
     */
    List<String> getRoleCodeList(String spaceBid, String userNo);

    /**
     * 根据空间和角色编码获取角色bid
     *
     * @param spaceBid 空间bid
     * @param roleCode 角色编码
     * @return String
     */
    String getSpaceRoleBid(String spaceBid, String roleCode);

    /**
     * 查询用户所在部门列表
     *
     * @param jobNum     jobNum
     * @return 返回值
     */
    ThreeDeptVO queryThreeDeptInfo(String jobNum);


    /**
     * 获取某个code下所有的code
     *
     * @param roleCode 角色编码
     * @return List<String>
     */
    List<String> getAllChildCode(String roleCode);

    /**
     * 获取所有子级的bid
     * @param bid 数据bid
     * @return 数据结果
     */
    List<String> getAllChildBid(String bid);
}
