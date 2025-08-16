package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowNodeDirectionConditionAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDirectionCondition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmFlowNodeDirectionConditionConverter {
    ApmFlowNodeDirectionConditionConverter INSTANCE = Mappers.getMapper(ApmFlowNodeDirectionConditionConverter.class);
    /**
     * 将ApmFlowNodeDirectionConditionAO对象转换为ApmFlowNodeDirectionCondition对象。
     *
     * @param ao ApmFlowNodeDirectionConditionAO对象
     * @return 转换后的ApmFlowNodeDirectionCondition对象
     */
    ApmFlowNodeDirectionCondition ao2Entity(ApmFlowNodeDirectionConditionAO ao);
}
