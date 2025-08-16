package com.transcend.plm.configcenter.role.pojo;

import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRoleUserPo;
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
public interface CfgRoleUserConverter {

    CfgRoleUserConverter INSTANCE = Mappers.getMapper(CfgRoleUserConverter.class);



    List<CfgRoleUserVo> po2vo(List<CfgRoleUserPo> po);
}
