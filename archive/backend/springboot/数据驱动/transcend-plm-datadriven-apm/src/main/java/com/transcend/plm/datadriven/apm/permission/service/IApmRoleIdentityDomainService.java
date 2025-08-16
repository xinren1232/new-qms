package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmCopyRoleAndIdentityQo;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAddAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;

import java.util.List;

/**
 * @author unknown
 */
public interface IApmRoleIdentityDomainService {
    /**
     * saveOrUpdate
     *
     * @param apmRoleIdentityAddAO apmRoleIdentityAddAO
     * @return {@link TranscendApiResponse<Boolean>}
     */
    TranscendApiResponse<Boolean> saveOrUpdate(ApmRoleIdentityAddAO apmRoleIdentityAddAO);

    /**
     * listByRoleBid
     *
     * @param roleBid roleBid
     * @param type    type
     * @param name    name
     * @return {@link List<ApmRoleIdentityVO>}
     */
    List<ApmRoleIdentityVO> listByRoleBid(String roleBid, String type, String name);

    /**
     * 空间角色标识列表
     *
     * @param spaceBid 空间bid
     * @param roleCode 角色编码
     * @param type     类型。employee：用户，department：部门
     * @param name     名称模糊搜索
     * @return 标识信息列表
     */
    List<ApmRoleIdentityVO> spaceIdentityList(String spaceBid, String roleCode, String type, String name);


    /**
     * 根据角色id 批量获取用户工号
     *
     * @param roleBids 角色ID集合
     * @param type     类型  employee：用户，department：部门
     * @return {@link List< String>} 工号集合
     * @date 2024/2/20 10:21
     * @author quan.cheng
     */
    List<String> listByRoleBids(List<String> roleBids, String type);

    /**
     * 通知查询角色人员
     *
     * @param notifyRoleCodes 角色编码集合
     * @param instanceBid     instanceBid
     * @param spaceAppBid     spaceAppBid
     * @param spaceBid        spaceBid
     * @return {@link List<String>}
     */
    List<String> listNotifyJobNumbers(List<String> notifyRoleCodes, String instanceBid, String spaceAppBid, String spaceBid);

    /**
     * deleteById
     *
     * @param id id
     * @return {@link boolean}
     */
    boolean deleteById(Integer id);

    /**
     * 复制角色和成员
     *
     * @param qo qo
     * @return
     */
    void copyRoleAndIdentity(ApmCopyRoleAndIdentityQo qo);

    /**
     * remove
     *
     * @param apmRoleIdentityDto apmRoleIdentityDto
     * @return {@link boolean}
     */
    boolean remove(ApmRoleIdentityDto apmRoleIdentityDto);

    /**
     * listByRole
     *
     * @param apmRoleQO apmRoleQO
     * @return {@link List<ApmRoleIdentityVO>}
     */
    List<ApmRoleIdentityVO> listByRole(ApmRoleQO apmRoleQO);


    /**
     * 人员月投入占比统计
     *
     * @return {@link Boolean}
     */
    Boolean personMonthInputStat();
}
