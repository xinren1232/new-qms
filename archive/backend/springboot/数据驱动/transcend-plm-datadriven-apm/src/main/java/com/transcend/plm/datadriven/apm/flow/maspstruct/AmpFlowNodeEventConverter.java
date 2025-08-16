package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowNodeEventAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowNodeEventVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowNodeEventConverter {
    AmpFlowNodeEventConverter INSTANCE = Mappers.getMapper(AmpFlowNodeEventConverter.class);

    /**
     * 将 ApmFlowNodeEventAO 对象转换为 ApmFlowNodeEvent 实体对象。
     *
     * @param apmFlowNodeEventAO 要转换的 ApmFlowNodeEventAO 对象
     * @return 转换后的 ApmFlowNodeEvent 实体对象
     */
    ApmFlowNodeEvent ao2Entity(ApmFlowNodeEventAO apmFlowNodeEventAO);

    /**
     * 将 ApmFlowNodeEvent 实体对象转换为 ApmFlowNodeEventVO 对象。
     *
     * @param apmFlowNodeEvent 要转换的 ApmFlowNodeEvent 实体对象
     * @return 转换后的 ApmFlowNodeEventVO 对象
     */
    ApmFlowNodeEventVO entity2VO(ApmFlowNodeEvent apmFlowNodeEvent);

    /**
     * 将 ApmFlowNodeEvent 实体对象列表转换为 ApmFlowNodeEventVO 对象列表。
     *
     * @param apmFlowNodeEvents 要转换的 ApmFlowNodeEvent 实体对象列表
     * @return 转换后的 ApmFlowNodeEventVO 对象列表
     */
    List<ApmFlowNodeEventVO> entitys2Vos(List<ApmFlowNodeEvent> apmFlowNodeEvents);
}
