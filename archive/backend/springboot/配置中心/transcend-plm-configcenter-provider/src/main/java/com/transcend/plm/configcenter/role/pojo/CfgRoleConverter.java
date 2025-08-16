package com.transcend.plm.configcenter.role.pojo;

import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRolePo;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
import com.transcend.plm.configcenter.role.pojo.vo.CfgRoleAndTypeVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgRoleConverter {

    CfgRoleConverter INSTANCE = Mappers.getMapper(CfgRoleConverter.class);

    /**
     * CfgRoleVo -> CfgRoleAndTypeVo
     * @param cfgRoleVo
     * @return
     */
    CfgRoleAndTypeVo vo2systemVo(CfgRoleVo cfgRoleVo);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgRolePo dto2po(CfgRoleDto dto);

    /**
     * do 转化为 vo
     * @param cfgAttribute
     * @return
     */
    CfgRoleVo po2vo(CfgRolePo cfgAttribute);

    /**
     * qo 转化为 po
     * @param cfgAttributeQo
     * @return
     */
    CfgRolePo qo2po(CfgRoleQo cfgAttributeQo);

    /**
     * do 转化为 vo
     * @param cfgAttributes
     * @return
     */
    List<CfgRoleVo> pos2vos(List<CfgRolePo> cfgAttributes);

    CfgRoleDto vo2dto(CfgRoleVo vo);
}
