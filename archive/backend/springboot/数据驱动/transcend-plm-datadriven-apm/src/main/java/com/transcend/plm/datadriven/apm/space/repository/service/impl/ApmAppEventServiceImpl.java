package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppEvent;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppEventService;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmAppEventMapper;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author peng.qin
* @description 针对表【apm_app_event】的数据库操作Service实现
* @createDate 2023-11-02 11:23:20
*/
@Service
public class ApmAppEventServiceImpl extends ServiceImpl<ApmAppEventMapper, ApmAppEvent>
    implements ApmAppEventService{


    @Override
    public boolean copyApmAppEvent(Map<String, String> appBidMap, Map<String, String> spaceBidMap) {
        if(CollectionUtils.isEmpty(appBidMap) || CollectionUtils.isEmpty(spaceBidMap)){
            return true;
        }
        List<ApmAppEvent> list = list(Wrappers.<ApmAppEvent>lambdaQuery().in(ApmAppEvent::getAppBid,appBidMap.keySet()));
        if(CollectionUtils.isNotEmpty(list)){
            for(ApmAppEvent apmAppEvent:list){
                apmAppEvent.setId(null);
                apmAppEvent.setBid(SnowflakeIdWorker.nextIdStr());
                apmAppEvent.setAppBid(appBidMap.get(apmAppEvent.getAppBid()));
                apmAppEvent.setSpaceBid(spaceBidMap.get(apmAppEvent.getSpaceBid()));
            }
            saveBatch(list);
        }
        return false;
    }
}




