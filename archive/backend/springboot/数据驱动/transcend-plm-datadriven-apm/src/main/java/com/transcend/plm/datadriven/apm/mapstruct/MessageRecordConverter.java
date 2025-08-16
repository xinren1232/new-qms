package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.MessageRecordAO;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author yanjie
 * @Date 2024/1/3 17:18
 * @Version 1.0
 */

@Mapper
public interface MessageRecordConverter {

    MessageRecordConverter INSTANCE = Mappers.getMapper(MessageRecordConverter.class);

    /**
     * ao2Entity
     *
     * @param messageRecordAo messageRecordAo
     * @return {@link MessageRecord}
     */
    MessageRecord ao2Entity(MessageRecordAO messageRecordAo);
}
