package com.transcend.plm.datadriven.apm.flow.maspstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowInstanceNodeAO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceNodeVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateNodeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程实例节点转换器
 * @createTime 2023-10-07 11:43:00
 */
@Mapper
public interface ApmFlowInstanceNodeConverter {
    ApmFlowInstanceNodeConverter INSTANCE = Mappers.getMapper(ApmFlowInstanceNodeConverter.class);

    /**
     * 将ApmFlowTemplateNodeVO对象转换为ApmFlowInstanceNode对象。
     *
     * @param apmFlowTemplateNode 待转换的ApmFlowTemplateNodeVO对象
     * @return 转换后的ApmFlowInstanceNode对象
     */
    @Mappings({
            @Mapping(target = "templateNodeBid", source = "bid"),
            @Mapping(target = "templateNodeDataBid", source = "dataBid"),
            @Mapping(target = "id", ignore = true)
    })
    ApmFlowInstanceNode template2Entity(ApmFlowTemplateNodeVO apmFlowTemplateNode);

    /**
     * 将ApmFlowInstanceNode对象转换为ApmFlowInstanceNodeAO对象。
     *
     * @param po 待转换的ApmFlowInstanceNode对象
     * @return 转换后的ApmFlowInstanceNodeAO对象
     */
    ApmFlowInstanceNodeAO po2Ao(ApmFlowInstanceNode po);
    /**
     * 将ApmFlowInstanceNode对象转换为ApmFlowInstanceNodeAO对象。
     *
     * @param pos 待转换的ApmFlowInstanceNode对象
     * @return 转换后的ApmFlowInstanceNodeAO对象
     */
    List<ApmFlowInstanceNodeAO> pos2Aos(List<ApmFlowInstanceNode> pos);


    ApmFlowInstanceNodeVO po2Vo(ApmFlowInstanceNode v);
}
