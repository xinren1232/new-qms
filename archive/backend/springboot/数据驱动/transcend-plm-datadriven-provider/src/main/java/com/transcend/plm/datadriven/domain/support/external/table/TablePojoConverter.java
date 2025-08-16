package com.transcend.plm.datadriven.domain.support.external.table;

import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface TablePojoConverter {

    TablePojoConverter INSTANCE = Mappers.getMapper(TablePojoConverter.class);

    /**
     * cfg2definition
     *
     * @param dto
     * @return
     */
    TableDefinition cfg2definition(CfgTableVo dto);

}
