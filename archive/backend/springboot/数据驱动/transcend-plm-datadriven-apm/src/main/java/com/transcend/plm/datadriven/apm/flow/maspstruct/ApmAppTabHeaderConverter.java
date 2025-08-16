package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmAppTabHeaderVO;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmAppTabHeaderConverter {
    ApmAppTabHeaderConverter INSTANCE = Mappers.getMapper(ApmAppTabHeaderConverter.class);

    /**
     * 将ApmAppTabHeader实体对象转换为ApmAppTabHeaderVO对象
     *
     * @param entity ApmAppTabHeader实体对象
     * @return 转换后的ApmAppTabHeaderVO对象
     */
    ApmAppTabHeaderVO entityToVO(ApmAppTabHeader entity);
}
