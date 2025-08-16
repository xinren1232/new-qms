package com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service;

import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;

/**
 * @author unknown
 */
public interface INotifyMessageService {

    /**
     * pushMsg
     *
     * @param apmNotifyExecuteRecord apmNotifyExecuteRecord
     */
    void pushMsg(ApmNotifyExecuteRecord apmNotifyExecuteRecord);

}
