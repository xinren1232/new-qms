package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_role_identity】的数据库操作Mapper
* @createDate 2023-09-20 16:15:29
* @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmRoleIdentity
*/
public interface ApmRoleIdentityMapper extends BaseMapper<ApmRoleIdentity> {
    /**
     *
     * 方法描述
     * @param sphereBid sphereBid
     * @param type type
     * @param name name
     * @return 返回值
     */
    List<ApmRoleIdentity> listAllBySphereBid(@Param("sphereBid") String sphereBid, @Param("type") String type, @Param("name") String name);

    /**
     *
     * 方法描述
     * @param sphereType sphereType
     * @param bizBid bizBid
     * @param codes codes
     * @return 返回值
     */
    List<ApmRoleIdentity> listEmpByBizBidAndCodes(@Param("sphereType") String sphereType, @Param("bizBid") String bizBid, @Param("codes") List<String> codes);

    /**
     *
     * 方法描述
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRoleIdentity> listAllRoleAndIdentityBySphereBid(@Param("sphereBid") String sphereBid);

    /**
     *
     * 方法描述
     * @param apmSphereBids apmSphereBids
     * @return 返回值
     */
    List<ApmRoleIdentity> listInputPercentageNotNull(@Param("sphereBids") Set<String> apmSphereBids);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityDto apmRoleIdentityDto
     * @return 返回值
     */
    boolean physicsRemove(@Param("roleIdentityDto") ApmRoleIdentityDto apmRoleIdentityDto);
}




