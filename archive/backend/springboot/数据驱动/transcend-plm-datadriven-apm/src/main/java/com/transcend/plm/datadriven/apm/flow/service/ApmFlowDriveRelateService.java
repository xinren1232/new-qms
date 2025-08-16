package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_flow_drive_relate】的数据库操作Service
* @createDate 2023-10-25 11:51:42
*/
public interface ApmFlowDriveRelateService extends IService<ApmFlowDriveRelate> {

    /**
     * 根据事件bid查询关联驱动配置
     * @param eventBid 事件bid
     * @return  关联驱动配置列表
     */
    List<ApmFlowDriveRelate> listByEventBid(String eventBid);
}
