package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowInstanceProcessDto;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceProcessPo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceProcessVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowTemplateProcessConverter {
    AmpFlowTemplateProcessConverter INSTANCE = Mappers.getMapper(AmpFlowTemplateProcessConverter.class);

    /**
     * 该方法用于将ApmFlowInstanceProcessPo对象列表转换为ApmFlowInstanceProcessVo对象列表。
     *
     * @param poList ApmFlowInstanceProcessPo对象列表，要转换的持久化对象列表。
     * @return ApmFlowInstanceProcessVo对象列表，转换后的数据传输对象列表。
     */
    List<ApmFlowInstanceProcessVo> pos2vos(List<ApmFlowInstanceProcessPo> poList);

    /**
     * 该方法用于将ApmFlowInstanceProcessDto对象转换为ApmFlowInstanceProcessPo对象。
     *
     * @param dto ApmFlowInstanceProcessDto对象，要转换的数据传输对象。
     * @return ApmFlowInstanceProcessPo对象，转换后的持久化对象。
     */
    ApmFlowInstanceProcessPo dto2po(ApmFlowInstanceProcessDto dto);
}
