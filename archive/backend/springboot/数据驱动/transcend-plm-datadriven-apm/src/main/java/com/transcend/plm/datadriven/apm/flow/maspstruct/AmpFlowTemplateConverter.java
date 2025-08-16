package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowTemplateAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface AmpFlowTemplateConverter {
    AmpFlowTemplateConverter INSTANCE = Mappers.getMapper(AmpFlowTemplateConverter.class);

    /**
     * 将ApmFlowTemplateAO对象转换为ApmFlowTemplate对象。
     *
     * @param apmFlowTemplateAO ApmFlowTemplateAO对象，需要被转换的源对象
     * @return 转换后的ApmFlowTemplate对象
     */
    ApmFlowTemplate ao2Entity(ApmFlowTemplateAO apmFlowTemplateAO);

    /**
     * 将给定的ApmFlowTemplate对象转换为ApmFlowTemplateVO对象。
     *
     * @param apmFlowTemplate ApmFlowTemplate对象，需要被转换的源对象
     * @return 转换后的ApmFlowTemplateVO对象
     */
    ApmFlowTemplateVO entity2VO(ApmFlowTemplate apmFlowTemplate);

    /**
     * 将给定的ApmFlowTemplate对象列表转换为ApmFlowTemplateVO对象列表。
     *
     * @param apmFlowTemplateList ApmFlowTemplate对象列表，需要被转换的源对象列表
     * @return 转换后的ApmFlowTemplateVO对象列表
     */
    List<ApmFlowTemplateVO> entitys2Vos(List<ApmFlowTemplate> apmFlowTemplateList);
}
