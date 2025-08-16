package com.transcend.plm.datadriven.domain.support.external.table;

import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface TableAttritubePojoConverter {

    TableAttritubePojoConverter INSTANCE = Mappers.getMapper(TableAttritubePojoConverter.class);

    /**
     * cfg2definition
     *
     * @param vo
     * @return
     */
    TableAttributeDefinition cfg2definition(CfgTableAttributeVo vo);

    /**
     * cfg2definition
     *
     * @param vos
     * @return List<TableAttributeDefinition>
     */
    List<TableAttributeDefinition> cfg2definitions(List<CfgTableAttributeVo> vos);

}
