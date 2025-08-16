package com.transcend.plm.datadriven.apm.mapstruct;

import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecordHis;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author unknown
 */
@Mapper
public interface ApmNotifyExecuteRecordHisConverter {
    ApmNotifyExecuteRecordHisConverter INSTANCE = Mappers.getMapper(ApmNotifyExecuteRecordHisConverter.class);

    /**
     *
     * 方法描述
     * @param apmNotifyExecuteRecord apmNotifyExecuteRecord
     * @return 返回值
     */
    ApmNotifyExecuteRecordHis po2His(ApmNotifyExecuteRecord apmNotifyExecuteRecord);
}
