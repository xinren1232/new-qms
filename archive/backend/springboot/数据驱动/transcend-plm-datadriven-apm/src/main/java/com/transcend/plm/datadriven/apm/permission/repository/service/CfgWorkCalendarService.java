package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.CfgWorkCalendar;

import java.util.Date;

/**
 * @author unknown
 */
public interface CfgWorkCalendarService extends IService<CfgWorkCalendar> {

    /**
     * 判断工作日天数
     * @param startDate
     * @param endDate
     * @return
     */
    long getWorkDays(Date startDate, Date endDate);
}
