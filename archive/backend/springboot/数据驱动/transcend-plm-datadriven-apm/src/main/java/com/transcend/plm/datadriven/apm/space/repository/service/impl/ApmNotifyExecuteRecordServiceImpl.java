package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmNotifyExecuteRecordMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmNotifyExecuteRecordServiceImpl extends ServiceImpl<ApmNotifyExecuteRecordMapper, ApmNotifyExecuteRecord> implements ApmNotifyExecuteRecordService {

    @Override
    public int deleteById(int id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public List<ApmNotifyExecuteRecord> selectAllExecuteRecord() {
        String endOfDayS = getEndOfDayS();
        return baseMapper.selectAllExecuteRecord(endOfDayS);
    }

    @Override
    public boolean updateStatusById(List<ApmNotifyExecuteRecord> apmNotifyExecuteRecords) {
        return updateBatchById(apmNotifyExecuteRecords);
    }

    @Override
    public List<ApmNotifyExecuteRecord> listByInstanceBids(List<String> instanceBids, String spaceAppBid) {
        List<ApmNotifyExecuteRecord> list = list(Wrappers.<ApmNotifyExecuteRecord>lambdaQuery().in(ApmNotifyExecuteRecord::getInstanceBid, instanceBids).eq(ApmNotifyExecuteRecord::getSpaceAppBid, spaceAppBid));
        return list;
    }

    @Override
    public List<ApmNotifyExecuteRecord> selectImmediateExecuteRecord() {
        return baseMapper.selectImmediateExecuteRecord();
    }

    @Override
    public List<ApmNotifyExecuteRecord> selectExecuteRecordByParam(String nowTime, String type) {
        return baseMapper.selectExecuteRecordByParam(nowTime, type);
    }

    /**
     * 获取当前日期的结束时间（23:59:59.999999999）
     *
     * @return 当前日期的结束时间（23:59:59.999999999）
     */
    @NotNull
    private static String getEndOfDayS() {
        // 当前时间（LocalDate方法
        LocalDate currentLD = LocalDate.now();
        // 获取当前日期的结束时间（23:59:59.999999999）
        LocalDateTime endOfDayLD = currentLD.atTime(LocalTime.MAX);
        // LocalDateTime转Date
        Date endOfDayD = java.sql.Timestamp.valueOf(endOfDayLD);
        // endOfDayD转String
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(endOfDayD);
    }
}




