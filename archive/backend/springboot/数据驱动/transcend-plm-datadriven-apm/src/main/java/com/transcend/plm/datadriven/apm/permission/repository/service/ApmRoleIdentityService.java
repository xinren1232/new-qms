package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;

import java.util.List;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_role_identity】的数据库操作Service
* @createDate 2023-09-20 16:15:30
*/
public interface ApmRoleIdentityService extends IService<ApmRoleIdentity> {
    /**
     *
     * 方法描述
     * @param roleBids roleBids
     * @return 返回值
     */
    List<ApmRoleIdentity> listByRoleBids(List<String> roleBids);

    /**
     *
     * 方法描述
     * @param sphereBid sphereBid
     * @param type type
     * @param name name
     * @return 返回值
     */
    List<ApmRoleIdentity> listAllBySphereBid(String sphereBid, String type, String name);

    /**
     *
     * 方法描述
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRoleIdentity> listAllRoleAndIdentityBySphereBid(String sphereBid);

    /**
     *
     * 方法描述
     * @param apmSphereBids apmSphereBids
     * @return 返回值
     */
    List<ApmRoleIdentity> listInputPercentageNotNull(Set<String> apmSphereBids);

    /**
     * 根据外部唯一标识获取角色身份信息
     * @param foreignBid
     * @return
     */
    ApmRoleIdentityVO getApmRoleIdentityVOByForeignBid(String foreignBid);

    /**
     * 根据roleBids删除角色身份信息
     * @param roleBids
     * @return
     */
    boolean removeByRoleBids(List<String> roleBids);

    /**
     *
     * 方法描述
     * @param apmRoleIdentity apmRoleIdentity
     * @param roleBids roleBids
     * @return 返回值
     */
    boolean removeByCondition(ApmRoleIdentity apmRoleIdentity, List<String> roleBids);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityDto apmRoleIdentityDto
     * @return 返回值
     */
    boolean physicsRemove(ApmRoleIdentityDto apmRoleIdentityDto);

    /**
     *
     * 方法描述
     * @param sphereType sphereType
     * @param bizBid bizBid
     * @param codes codes
     * @return 返回值
     */
    List<ApmRoleIdentity> listEmpByBizBidAndCodes(String sphereType, String bizBid,List<String> codes);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityAo apmRoleIdentityAo
     * @return 返回值
     */
    boolean saveRoleIdentity(ApmRoleIdentityAO apmRoleIdentityAo);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityAo apmRoleIdentityAo
     * @return 返回值
     */
    boolean updateRoleIdentity(ApmRoleIdentityAO apmRoleIdentityAo);
}
