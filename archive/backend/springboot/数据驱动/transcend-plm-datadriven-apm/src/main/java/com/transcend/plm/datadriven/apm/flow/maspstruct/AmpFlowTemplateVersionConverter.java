package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateVersion;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowTemplateVersionConverter {
    AmpFlowTemplateVersionConverter INSTANCE = Mappers.getMapper(AmpFlowTemplateVersionConverter.class);

    /**
     * 将ApmFlowTemplate对象转换为ApmFlowTemplateVersion对象。
     *
     * @param apmFlowTemplate 要转换的ApmFlowTemplate对象
     * @return 转换后的ApmFlowTemplateVersion对象
     */
    ApmFlowTemplateVersion temp2tempVersion(ApmFlowTemplate apmFlowTemplate);
}
