package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.CadBatchAddInstanceRecord;

import java.util.List;

/**
 * @author shu.zhang
 * @version 1.0
 * @className CadBatchAddInstanceRecordService
 * @description desc
 * @date 2024/8/16 15:42
 */
public interface CadBatchAddInstanceRecordService extends IService<CadBatchAddInstanceRecord> {

    /**
     * saveBatchRecord
     *
     * @param cadBatchAddInstanceRecords cadBatchAddInstanceRecords
     * @return {@link boolean}
     */
    boolean saveBatchRecord(List<CadBatchAddInstanceRecord> cadBatchAddInstanceRecords);

    /**
     * getByBid
     *
     * @param bid bid
     * @return {@link List<CadBatchAddInstanceRecord>}
     */
    List<CadBatchAddInstanceRecord> getByBid(String bid);
}
