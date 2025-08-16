package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowNodeDisplayConditionAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDisplayCondition;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowNodeDisplayConditionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowNodeDisplayConditionConverter {

    AmpFlowNodeDisplayConditionConverter INSTANCE = Mappers.getMapper(AmpFlowNodeDisplayConditionConverter.class);

    /**
     * 将ApmFlowNodeDisplayConditionAO对象转换为ApmFlowNodeDisplayCondition实体对象。
     *
     * @param apmFlowNodeDisplayConditionAO ApmFlowNodeDisplayConditionAO对象
     * @return 转换后的ApmFlowNodeDisplayCondition实体对象
     */
    ApmFlowNodeDisplayCondition ao2Entity(ApmFlowNodeDisplayConditionAO apmFlowNodeDisplayConditionAO);

    /**
     * 将ApmFlowNodeDisplayCondition实体对象转换为ApmFlowNodeDisplayConditionVO对象。
     *
     * @param apmFlowNodeDisplayCondition ApmFlowNodeDisplayCondition实体对象
     * @return 转换后的ApmFlowNodeDisplayConditionVO对象
     */
    ApmFlowNodeDisplayConditionVO entity2Vo(ApmFlowNodeDisplayCondition apmFlowNodeDisplayCondition);

    /**
     * 将ApmFlowNodeDisplayCondition实体对象列表转换为ApmFlowNodeDisplayConditionVO对象列表。
     *
     * @param apmFlowNodeDisplayConditions ApmFlowNodeDisplayCondition实体对象列表
     * @return 转换后的ApmFlowNodeDisplayConditionVO对象列表
     */
    List<ApmFlowNodeDisplayConditionVO> entitys2Vos(List<ApmFlowNodeDisplayCondition> apmFlowNodeDisplayConditions);
}
