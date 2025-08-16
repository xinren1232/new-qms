package com.transcend.plm.datadriven.apm.statistics.repository.service.ipml;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.statistics.repository.po.ApmStatisticRecord;
import com.transcend.plm.datadriven.apm.statistics.repository.service.ApmStatisticRecordService;
import com.transcend.plm.datadriven.apm.statistics.repository.mapper.ApmStatisticRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author jinpeng.bai
* @description 针对表【apm_statistic_record(统计记录表)】的数据库操作Service实现
* @createDate 2023-10-25 17:11:50
*/
@Service
public class ApmStatisticRecordServiceImpl extends ServiceImpl<ApmStatisticRecordMapper, ApmStatisticRecord>
    implements ApmStatisticRecordService{

}




