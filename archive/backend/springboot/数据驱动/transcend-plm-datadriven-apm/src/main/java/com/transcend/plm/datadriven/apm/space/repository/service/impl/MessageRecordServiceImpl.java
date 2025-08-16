package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MessageRecordAO;
import com.transcend.plm.datadriven.apm.mapstruct.MessageRecordConverter;
import com.transcend.plm.datadriven.apm.space.repository.mapper.MessageRecordMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.MessageRecordService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Author yanjie
 * @Date 2024/1/3 16:44
 * @Version 1.0
 */
@Service
public class MessageRecordServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord>
        implements MessageRecordService {


    @Resource
    MessageRecordMapper messageRecordMapper;

    @Override
    public boolean add(MessageRecordAO messageRecordAo) {
        if (Objects.isNull(messageRecordAo)) {
            return false;
        }
        MessageRecord messageRecord = MessageRecordConverter.INSTANCE.ao2Entity(messageRecordAo);
        return save(messageRecord);
    }

    @Override
    public boolean physicsRemoveByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        return messageRecordMapper.physicsRemoveByIds(ids);
    }
}
