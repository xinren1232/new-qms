package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectPermissionSaveParam;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-25 11:06
 **/
@Mapper
public interface CfgObjectPermissionConverter {


    CfgObjectPermissionConverter INSTANCE = Mappers.getMapper(CfgObjectPermissionConverter.class);

    /**
     * po 转化为 vo
     * @param po
     * @return
     */
    CfgObjectPermissionVo po2vo(CfgObjectPermissionPo po);

    /**
     * pos 转化为 vos
     * @param po
     * @return
     */
    List<CfgObjectPermissionVo> pos2vos(List<CfgObjectPermissionPo> po);
    /**
     * vo 转化为 po
     * @param vo
     * @return
     */
    CfgObjectPermissionPo vo2po(CfgObjectPermissionVo vo);


    CfgObjectPermissionPo saveParam2po(CfgObjectPermissionSaveParam saveParam);

    CfgObjectPermissionPo qo2po(CfgObjectPermissionQo param);
}
