package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataDeleteEvent;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除关联特性时要删除与IR关系
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/15 16:19
 */
@Service
@AllArgsConstructor
public class SystemFeatureDeleteRelationHandler {

    private final ObjectModelStandardI<MObject> objectModelCrudI;

    @Resource
    private SystemFeatureService systemFeatureService;


    /**
     * 不匹配判定
     *
     * @param modelCode 模型编码
     * @return 是否匹配
     */
    private static boolean nonMatch(String modelCode) {
        return !TranscendModel.matchCode(modelCode, TranscendModel.RSF);
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataDeleteEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }
        List<MObject> mObjectList = objectModelCrudI.list(modelCode, event.getWrappers());
        if (CollectionUtils.isNotEmpty(mObjectList)){
            List<String> bids = mObjectList.stream().map(MObject::getBid).collect(Collectors.toList());
            systemFeatureService.deleteIrRsfRelation(bids);
        }
    }

}
