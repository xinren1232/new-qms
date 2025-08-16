package com.transcend.plm.configcenter.permission.pojo;

import com.transcend.plm.configcenter.permission.infrastructure.repository.po.CfgPermissionPo;
import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgAttributeConverter {

    CfgAttributeConverter INSTANCE = Mappers.getMapper(CfgAttributeConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgPermissionPo dto2po(CfgObjectPermissionOperationDto dto);

    /**
     * do 转化为 vo
     * @param cfgPermissionPo
     * @return
     */
    CfgAttributeVo po2vo(CfgPermissionPo cfgPermissionPo);

    /**
     * qo 转化为 po
     * @param cfgAttributeQo
     * @return
     */
    CfgPermissionPo qo2po(CfgObjectPermissionOperationQo cfgAttributeQo);

    /**
     * do 转化为 vo
     * @param cfgPermissionPos
     * @return
     */
    List<CfgAttributeVo> pos2vos(List<CfgPermissionPo> cfgPermissionPos);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgPermissionPo> dtos2pos(List<CfgObjectPermissionOperationDto> cfgAttributeDtos);
}
