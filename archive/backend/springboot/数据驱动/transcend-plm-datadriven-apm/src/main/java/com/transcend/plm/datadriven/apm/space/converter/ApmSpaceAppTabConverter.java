package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppTabDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmSpaceAppTabConverter {
    ApmSpaceAppTabConverter INSTANCE = Mappers.getMapper(ApmSpaceAppTabConverter.class);

    /**
     * dto2entity
     *
     * @param apmSpaceAppTabDto apmSpaceAppTabDto
     * @return ApmSpaceAppTab
     */
    ApmSpaceAppTab dto2entity(ApmSpaceAppTabDto apmSpaceAppTabDto);
}
