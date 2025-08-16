package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmRoleBO;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmRoleConverter {
    ApmRoleConverter INSTANCE = Mappers.getMapper(ApmRoleConverter.class);

    /**
     *
     * 方法描述
     * @param apmRoleAO apmRoleAO
     * @return 返回值
     */
    ApmRole ao2Entity(ApmRoleAO apmRoleAO);

    /**
     *
     * 方法描述
     * @param apmRoleDto apmRoleDto
     * @return 返回值
     */
    ApmRole dto2Entity(ApmRoleDto apmRoleDto);

    /**
     *
     * 方法描述
     * @param apmRole apmRole
     * @return 返回值
     */
    ApmRoleVO entity2VO(ApmRole apmRole);

    /**
     *
     * 方法描述
     * @param apmRoles apmRoles
     * @return 返回值
     */
    List<ApmRoleVO> entityList2VOList(List<ApmRole> apmRoles);

    /**
     *
     * 方法描述
     * @param apmRoleQO apmRoleQO
     * @return 返回值
     */
    ApmRole qo2Entity(ApmRoleQO apmRoleQO);

    /**
     *
     * 方法描述
     * @param apmRole apmRole
     * @return 返回值
     */
    ApmRoleBO entity2BO(ApmRole apmRole);

    /**
     *
     * 方法描述
     * @param list list
     * @return 返回值
     */
    List<ApmRoleVO> entitys2vos(List<ApmRole> list);
}
