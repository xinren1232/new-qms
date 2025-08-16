package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowNodeLineAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeLine;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmFlowNodeLineConverter {
    ApmFlowNodeLineConverter INSTANCE = Mappers.getMapper(ApmFlowNodeLineConverter.class);
    /**
     * 将ApmFlowNodeLineAO对象转换为ApmFlowNodeLine对象。
     *
     * @param ao 要转换的ApmFlowNodeLineAO对象。
     * @return 转换后的ApmFlowNodeLine对象。
     */
    ApmFlowNodeLine ao2Po(ApmFlowNodeLineAO ao);

}

