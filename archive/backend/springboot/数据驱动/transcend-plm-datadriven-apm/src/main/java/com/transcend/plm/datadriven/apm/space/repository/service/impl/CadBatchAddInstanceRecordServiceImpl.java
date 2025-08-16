package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.mapper.CadBatchAddInstanceRecordMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.CadBatchAddInstanceRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.CadBatchAddInstanceRecordService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author shu.zhang
 * @version 1.0
 * @className CadBatchAddInstanceRecordServiceImpl
 * @description desc
 * @date 2024/8/16 15:43
 */
@Service
public class CadBatchAddInstanceRecordServiceImpl extends ServiceImpl<CadBatchAddInstanceRecordMapper, CadBatchAddInstanceRecord>
        implements CadBatchAddInstanceRecordService {


    @Override
    public boolean saveBatchRecord(List<CadBatchAddInstanceRecord> cadBatchAddInstanceRecords) {
        if (CollectionUtils.isNotEmpty(cadBatchAddInstanceRecords)) {
            return this.saveBatch(cadBatchAddInstanceRecords);
        }
        return true;
    }

    @Override
    public List<CadBatchAddInstanceRecord> getByBid(String bid) {
        if (StringUtils.isNotBlank(bid)) {
            return this.list(Wrappers.<CadBatchAddInstanceRecord>lambdaQuery().eq(CadBatchAddInstanceRecord::getRequestId, bid));
        }
        return Collections.emptyList();
    }
}
