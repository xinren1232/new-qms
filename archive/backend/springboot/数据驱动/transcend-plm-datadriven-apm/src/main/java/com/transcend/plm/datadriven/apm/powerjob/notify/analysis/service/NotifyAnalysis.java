package com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;

import java.util.List;

/**
 * @author quan.cheng
 * @description 通知解析接口
 * @title NotifyAnalysis
 * @date 2024/2/4 16:00
 */
public interface NotifyAnalysis {

    /**
     * 解析通知规则生成需要发送的通知记录
     *
     * @param notifyConfigVos   通知配置信息
     * @param mSpaceAppDataList 空间应用数据
     * @param checkInstanceBid  是否校验实例id
     * @return list
     */
    List<ApmNotifyExecuteRecord> analysis(List<NotifyConfigVo> notifyConfigVos, List<MSpaceAppData> mSpaceAppDataList, boolean checkInstanceBid);

    /**
     * 发送前检查
     *
     * @param apmNotifyExecuteRecord 通知执行记录
     * @return boolean
     */
    Boolean sendBeforeCheck(ApmNotifyExecuteRecord apmNotifyExecuteRecord);
}
