package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAccessAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAccessVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmRoleAccessConverter {
    ApmRoleAccessConverter INSTANCE = Mappers.getMapper(ApmRoleAccessConverter.class);

    /**
     *
     * 方法描述
     * @param amRoleAccessAO amRoleAccessAO
     * @return 返回值
     */
    ApmRoleAccess ao2Entity(ApmRoleAccessAO amRoleAccessAO);

    /**
     *
     * 方法描述
     * @param amRoleAccessAOList amRoleAccessAOList
     * @return 返回值
     */
    List<ApmRoleAccess> aoList2EntityList(List<ApmRoleAccessAO> amRoleAccessAOList);

    /**
     *
     * 方法描述
     * @param apmRoleAccess apmRoleAccess
     * @return 返回值
     */
    ApmRoleAccessVO entity2VO(ApmRoleAccess apmRoleAccess);

    /**
     *
     * 方法描述
     * @param apmRoleAccesses apmRoleAccesses
     * @return 返回值
     */
    List<ApmRoleAccessVO> entityList2VOList(List<ApmRoleAccess> apmRoleAccesses);
}
