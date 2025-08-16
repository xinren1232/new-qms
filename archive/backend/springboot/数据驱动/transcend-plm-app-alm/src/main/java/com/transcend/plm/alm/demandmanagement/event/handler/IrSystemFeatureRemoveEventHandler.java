package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationBatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationBatchRemoveEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class IrSystemFeatureRemoveEventHandler extends AbstractRelationBatchRemoveEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Override
    public RelationBatchDeleteEventHandlerParam preHandle(RelationBatchDeleteEventHandlerParam param) {
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(RelationBatchDeleteEventHandlerParam param, Boolean result) {
        //查询是否还存在关联的安全信息数据
        RelationDelAndRemParamAo relationDelAndRemParamAo = param.getRelationDelAndRemParamAo();
        //更新关联4级特性属性
        MObject mObject = objectModelCrudI.getByBid(TranscendModel.IR.getCode(), relationDelAndRemParamAo.getSourceBid());
        if (mObject != null) {
            Object mountSf = mObject.get(SystemFeatureConstant.MOUNT_SF);
            if (mountSf instanceof List) {
                List<Object> mountSFBidObject = (List<Object>) mountSf;
                List<String> finalRsfBids = relationDelAndRemParamAo.getTargetBids();
                mountSFBidObject.removeIf(v->{
                    String bid = null;
                    if (v instanceof String){
                        bid =  v.toString();
                    } else if (v instanceof List){
                        bid =  ((List<?>)v).get(((List<?>) v).size()-1).toString();
                    }
                    return bid != null && finalRsfBids.contains(bid);
                });
            }
            //修改实例属性
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.put(SystemFeatureConstant.MOUNT_SF, mountSf);
            mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(),false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(relationDelAndRemParamAo.getSourceSpaceAppBid(), relationDelAndRemParamAo.getSourceBid(), mSpaceAppData);
        }
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationBatchDeleteEventHandlerParam param) {
        RelationDelAndRemParamAo relationDelAndRemParamAo = param.getRelationDelAndRemParamAo();
        return TranscendModel.RELATION_IR_RSF.getCode().equals(relationDelAndRemParamAo.getModelCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
