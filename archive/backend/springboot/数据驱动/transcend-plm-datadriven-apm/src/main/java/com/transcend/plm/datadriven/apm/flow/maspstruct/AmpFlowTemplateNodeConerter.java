package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowTemplateNodeAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateNodeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowTemplateNodeConerter {
    AmpFlowTemplateNodeConerter INSTANCE = Mappers.getMapper(AmpFlowTemplateNodeConerter.class);

    /**
     * 将给定的ApmFlowTemplateNodeAO对象转换为ApmFlowTemplateNode实体对象。
     *
     * @param apmFlowTemplateNodeAO 给定的ApmFlowTemplateNodeAO对象
     * @return 转换后的ApmFlowTemplateNode实体对象
     */
    ApmFlowTemplateNode ao2Entity(ApmFlowTemplateNodeAO apmFlowTemplateNodeAO);

    /**
     * 将ApmFlowTemplateNode实体对象转换为ApmFlowTemplateNodeVO对象。
     *
     * @param apmFlowTemplateNode ApmFlowTemplateNode实体对象
     * @return ApmFlowTemplateNodeVO对象
     */
    ApmFlowTemplateNodeVO entity2VO(ApmFlowTemplateNode apmFlowTemplateNode);

    /**
     * 将ApmFlowTemplateNode实体对象列表转换为ApmFlowTemplateNodeVO对象列表。
     *
     * @param apmFlowTemplateNodeList ApmFlowTemplateNode实体对象列表
     * @return ApmFlowTemplateNodeVO对象列表
     */
    List<ApmFlowTemplateNodeVO> entitys2Vos(List<ApmFlowTemplateNode> apmFlowTemplateNodeList);
}
