package com.transcend.plm.datadriven.domain.support.external.object;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.datadriven.api.model.config.ObjectAttributeVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface ObjectAttritubePojoConverter {

    ObjectAttritubePojoConverter INSTANCE = Mappers.getMapper(ObjectAttritubePojoConverter.class);

    /**
     * cfg2definition
     *
     * @param vo
     * @return
     */
    ObjectAttributeVo cfg2definition(CfgObjectAttributeVo vo);

    /**
     * cfg2definition
     * @param vos
     * @return List<ObjectAttributeVo>
     */
    List<ObjectAttributeVo> cfg2definitions(List<CfgObjectAttributeVo> vos);

}
