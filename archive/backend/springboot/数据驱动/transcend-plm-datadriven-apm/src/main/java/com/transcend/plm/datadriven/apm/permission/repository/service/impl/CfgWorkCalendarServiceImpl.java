package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.CfgWorkCalendar;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.CfgWorkCalendarMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.CfgWorkCalendarService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author unknown
 */
@Service
public class CfgWorkCalendarServiceImpl extends ServiceImpl<CfgWorkCalendarMapper, CfgWorkCalendar>
        implements CfgWorkCalendarService {

    @Override
    public long getWorkDays(Date startDate, Date endDate) {
        long workDays = count(Wrappers.<CfgWorkCalendar>lambdaQuery().ge(CfgWorkCalendar::getWorkTime, startDate).le(CfgWorkCalendar::getWorkTime, endDate).eq(CfgWorkCalendar::getZtpr, "ON"));
        return workDays;
    }
}




