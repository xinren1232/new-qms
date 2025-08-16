package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowDriveRelateService;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowDriveRelateMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_flow_drive_relate】的数据库操作Service实现
* @createDate 2023-10-25 11:51:42
*/
@Service
public class ApmFlowDriveRelateServiceImpl extends ServiceImpl<ApmFlowDriveRelateMapper, ApmFlowDriveRelate>
    implements ApmFlowDriveRelateService{

    @Override
    public List<ApmFlowDriveRelate> listByEventBid(String eventBid) {
        return this.list(Wrappers.<ApmFlowDriveRelate>lambdaQuery().eq(ApmFlowDriveRelate::getEventBid,eventBid));
    }
}




