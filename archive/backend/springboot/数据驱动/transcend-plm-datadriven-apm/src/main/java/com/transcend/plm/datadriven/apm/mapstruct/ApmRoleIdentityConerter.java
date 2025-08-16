package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmRoleIdentityConerter {
    ApmRoleIdentityConerter INSTANCE = Mappers.getMapper(ApmRoleIdentityConerter.class);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityAO apmRoleIdentityAO
     * @return 返回值
     */
    ApmRoleIdentity ao2Entity(ApmRoleIdentityAO apmRoleIdentityAO);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityDto apmRoleIdentityDto
     * @return 返回值
     */
    ApmRoleIdentity dto2Entity(ApmRoleIdentityDto apmRoleIdentityDto);

    /**
     *
     * 方法描述
     * @param apmRoleIdentityAOS apmRoleIdentityAOS
     * @return 返回值
     */
    List<ApmRoleIdentity> aoList2EntityList(List<ApmRoleIdentityAO> apmRoleIdentityAOS);

    /**
     *
     * 方法描述
     * @param apmRoleIdentity apmRoleIdentity
     * @return 返回值
     */
    ApmRoleIdentityVO entity2VO(ApmRoleIdentity apmRoleIdentity);

    /**
     *
     * 方法描述
     * @param apmRoleIdentities apmRoleIdentities
     * @return 返回值
     */
    List<ApmRoleIdentityVO> entityList2VOList(List<ApmRoleIdentity> apmRoleIdentities);
}
