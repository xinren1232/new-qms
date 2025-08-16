package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MessageRecordAO;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;

import java.util.List;

/**
 * @author unknown
 */
public interface MessageRecordService extends IService<MessageRecord> {
    /**
     * add
     *
     * @param messageRecordAO messageRecordAO
     * @return {@link boolean}
     */
    boolean add(MessageRecordAO messageRecordAO);

    /**
     * physicsRemoveByIds
     *
     * @param ids ids
     * @return {@link boolean}
     */
    boolean physicsRemoveByIds(List<Long> ids);
}
