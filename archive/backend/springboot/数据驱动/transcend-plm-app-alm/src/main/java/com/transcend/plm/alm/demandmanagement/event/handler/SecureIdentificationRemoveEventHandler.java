package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.event.entity.RelationBatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationBatchRemoveEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SecureIdentificationRemoveEventHandler extends AbstractRelationBatchRemoveEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private IBaseApmSpaceAppDataDrivenService baseApmSpaceAppDataDrivenService;

    @Value("${transcend.plm.apm.moudle.storySecureModelCode:A64}")
    private String storySecureModelCode;

    @Value("${transcend.plm.apm.moudle.featuresSecureModelCode:A5W}")
    private String featuresSecureModelCode;
    @Override
    public RelationBatchDeleteEventHandlerParam preHandle(RelationBatchDeleteEventHandlerParam param) {
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(RelationBatchDeleteEventHandlerParam param, Boolean result) {
        //查询是否还存在关联的安全信息数据
        RelationDelAndRemParamAo relationDelAndRemParamAo = param.getRelationDelAndRemParamAo();
        QueryWrapper resQo = new QueryWrapper();
        String sourceBid = relationDelAndRemParamAo.getSourceBid();
        String relationModelCode = relationDelAndRemParamAo.getModelCode();
        String sourceSpaceAppBid = relationDelAndRemParamAo.getSourceSpaceAppBid();
        resQo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> relQueryWrappers = QueryWrapper.buildSqlQo(resQo);
        List<MObject> allRelationList = objectModelCrudI.listByQueryWrapper(relationModelCode, relQueryWrappers);
        if (CollectionUtils.isEmpty(allRelationList)) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.setBid(sourceBid);
            mSpaceAppData.put("isRecordLog",false);
            mSpaceAppData.put("isItSafeToIdentify", "False");
            baseApmSpaceAppDataDrivenService.updatePartialContent(sourceSpaceAppBid, sourceBid, mSpaceAppData);
        }
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationBatchDeleteEventHandlerParam param) {
        RelationDelAndRemParamAo relationDelAndRemParamAo = param.getRelationDelAndRemParamAo();
        return storySecureModelCode.equals(relationDelAndRemParamAo.getModelCode()) || featuresSecureModelCode.equals(relationDelAndRemParamAo.getModelCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
