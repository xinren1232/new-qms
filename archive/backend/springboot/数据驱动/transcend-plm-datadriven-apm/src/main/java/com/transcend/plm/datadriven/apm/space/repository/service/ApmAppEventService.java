package com.transcend.plm.datadriven.apm.space.repository.service;

import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppEvent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author peng.qin
* @description 针对表【apm_app_event】的数据库操作Service
* @createDate 2023-11-02 11:23:20
*/
public interface ApmAppEventService extends IService<ApmAppEvent> {

    /**
     * 拷贝
     * @param appBidMap
     * @param spaceBidMap
     * @return
     */
    boolean copyApmAppEvent(Map<String,String> appBidMap, Map<String,String> spaceBidMap);
}
