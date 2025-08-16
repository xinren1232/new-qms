package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
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
public interface CfgObjectAttributeConverter {


    CfgObjectAttributeConverter INSTANCE = Mappers.getMapper(CfgObjectAttributeConverter.class);


    List<CfgObjectAttributeVo> pos2vos(List<CfgObjectAttributePo> listByModelCodeList);
}
