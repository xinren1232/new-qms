package com.transcend.plm.datadriven.apm.extension.strategy;

import com.transcend.plm.datadriven.apm.dto.NotifyObjectPartialContentDto;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchAddBusDto;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchRemoveBusDto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 时间处理策略
 * @date 2024/04/25 14:38
 **/
public abstract class ObjectExtensionStrategy {
    Map<String, ObjectExtensionStrategy> strategyMap = new HashMap<>();

    public void register(){
    }

    public void batchAdd(NotifyRelationBatchAddBusDto notifyRelationBatchAddBusDto){
    }

    public void batchRemove(NotifyRelationBatchRemoveBusDto notifyRelationBatchAddBusDto){
    }

    public void updatePartialContent(NotifyObjectPartialContentDto notifyObjectPartialContentDto){
    }

    public ObjectExtensionStrategy getStrategy(String key) {
        return strategyMap.get(key);
    }
}
