package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RR
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/11 16:53
 */
@Component
@AllArgsConstructor
public class RrMarkRejectionHandler {
    private final ObjectModelStandardI<MObject> objectModelCrudI;

    @EventListener
    public void handle(BaseDataUpdateEvent event) {
        if (!TranscendModel.matchCode(event.getModelCode(), TranscendModel.RR)) {
            return;
        }

        //确认是有修改到状态
        TranscendObjectWrapper updateDate = new TranscendObjectWrapper(event.getData());
        String lifeCycleCode = updateDate.getLifeCycleCode();
        if (lifeCycleCode == null) {
            return;
        }

        //提交状态编码
        String submitLifeCycleCode = "SUBMIT";
        //关闭状态编码
        String closeLifeCycleCode = "CLOSE";
        //草稿状态编码
        String draftLifeCycleCode = "DRAFT";
        //字段名称
        String fieldName = "whetherToReturn";

        List<MObject> instanceList = objectModelCrudI.list(TranscendModel.RR.getCode(), event.getWrappers());
        instanceList.stream().map(TranscendObjectWrapper::new).forEach(instance -> {
            String currentLifeCycleCode = instance.getLifeCycleCode();
            if (lifeCycleCode.equals(currentLifeCycleCode)) {
                return;
            }

            Boolean markRejection = null;
            if (submitLifeCycleCode.equals(currentLifeCycleCode)) {
                //1、从提交直接到关闭，标记为退回 or 3、从提交到下一个非关闭的节点，取消掉退回标记
                markRejection = closeLifeCycleCode.equals(lifeCycleCode);
            } else if (!draftLifeCycleCode.equals(currentLifeCycleCode) && submitLifeCycleCode.equals(lifeCycleCode)) {
                //2、除开草稿外，从任意节点退回到提交则标记为退回
                markRejection = true;
            }

            if (markRejection != null) {
                updateDate.put(fieldName, markRejection);
            }
        });

    }
}
