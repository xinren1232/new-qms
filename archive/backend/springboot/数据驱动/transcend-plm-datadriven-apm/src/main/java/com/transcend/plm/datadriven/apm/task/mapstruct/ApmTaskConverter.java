package com.transcend.plm.datadriven.apm.task.mapstruct;

import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmTaskConverter {
    ApmTaskConverter INSTANCE = Mappers.getMapper(ApmTaskConverter.class);

    /**
     * entity2Vo
     *
     * @param apmTask apmTask
     * @return {@link ApmTaskVO}
     */
    ApmTaskVO entity2Vo(ApmTask apmTask);

    /**
     * entitys2Vos
     *
     * @param apmTasks apmTasks
     * @return {@link List<ApmTaskVO>}
     */
    List<ApmTaskVO> entitys2Vos(List<ApmTask> apmTasks);

}
