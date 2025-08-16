package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class SecureIdentificationAddEventHandler extends AbstractRelationAddEventHandler {

    @Value("${transcend.plm.apm.moudle.storySecureModelCode:A64}")
    private String storySecureModelCode;

    @Value("${transcend.plm.apm.moudle.featuresSecureModelCode:A5W}")
    private String featuresSecureModelCode;

    @Resource
    private IBaseApmSpaceAppDataDrivenService baseApmSpaceAppDataDrivenService;

    @Override
    public int getOrder() {
        return 0;
    }
    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        return super.preHandle(param);
    }
    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        String sourceSpaceAppBid = param.getApmSpaceApp().getBid();
        List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
            if (CollectionUtils.isNotEmpty(targetMObjects)) {
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.setBid(sourceBid);
                mSpaceAppData.put("isRecordLog",false);
                mSpaceAppData.put("isItSafeToIdentify", "True");
                baseApmSpaceAppDataDrivenService.updatePartialContent(sourceSpaceAppBid, sourceBid, mSpaceAppData);
            }
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if(addExpandAo == null){
            return false;
        }
        return storySecureModelCode.equals(addExpandAo.getRelationModelCode()) || featuresSecureModelCode.equals(addExpandAo.getRelationModelCode());
    }
}
