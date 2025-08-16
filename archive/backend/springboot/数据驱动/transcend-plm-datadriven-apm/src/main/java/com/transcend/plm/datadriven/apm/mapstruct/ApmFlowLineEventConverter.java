package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowLineEventAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowLineEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmFlowLineEventConverter {
    ApmFlowLineEventConverter INSTANCE = Mappers.getMapper(ApmFlowLineEventConverter.class);

    /**
     * 将ApmFlowLineEventAO转换为ApmFlowLineEvent对象。
     *
     * @param ao ApmFlowLineEventAO对象，要转换的AO对象
     * @return 转换后的ApmFlowLineEvent对象
     */
    ApmFlowLineEvent ao2Po(ApmFlowLineEventAO ao);

}
