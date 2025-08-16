package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowNodeDirectionAO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowNodeDirectionVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDirection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmFlowNodeDirectionConverter {
    ApmFlowNodeDirectionConverter INSTANCE = Mappers.getMapper(ApmFlowNodeDirectionConverter.class);
    /**
     * 将ApmFlowNodeDirectionAO对象转换为ApmFlowNodeDirection对象。
     *
     * @param ao ApmFlowNodeDirectionAO对象，表示要转换的对象。
     * @return 转换后的ApmFlowNodeDirection对象。
     */
    ApmFlowNodeDirection ao2Entity(ApmFlowNodeDirectionAO ao);

    /**
     * 将给定的ApmFlowNodeDirection列表转换为ApmFlowNodeDirectionVO列表。
     *
     * @param entityList 给定的ApmFlowNodeDirection列表
     * @return 转换后的ApmFlowNodeDirectionVO列表
     */
    List<ApmFlowNodeDirectionVO> entitys2VOs(List<ApmFlowNodeDirection> entityList);
}
