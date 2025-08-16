package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectTreeEnum;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.constants.FlowInstanceNodeProperties;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckConstant;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Describe IR关联特性处理
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class IrSystemFeatureRelAddEventHandler extends AbstractRelationAddEventHandler {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private SystemFeatureService systemFeatureService;


    @Resource
    private IAppDataService appDataService;

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
        if(CollectionUtils.isNotEmpty(targetMObjects)){
            Set<Object> l3SfBids = targetMObjects.stream().map(v->v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString()).collect(Collectors.toSet());
            List<MObject> l3Objects = objectModelCrudI.listByBids(new ArrayList<>(l3SfBids), TranscendModel.RSF.getCode());
            Set<Object> l2SfBids = l3Objects.stream().map(v -> String.valueOf(v.get(ObjectTreeEnum.PARENT_BID.getCode()))).collect(Collectors.toSet());
            if (l2SfBids.size() != 1) {
                throw new PlmBizException("只能关联同一个二级特性下的四级特性");
            }
        }
        return super.preHandle(param);
    }

    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
        if(CollectionUtils.isNotEmpty(targetMObjects)){
            //找到四级特性的所有父级bid
            Set<Object> l3SfBids = targetMObjects.stream().map(v->v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString()).collect(Collectors.toSet());
            List<MObject> l3Objects = objectModelCrudI.listByBids(new ArrayList<>(l3SfBids), TranscendModel.RSF.getCode());
            Set<Object> l2SfBids = l3Objects.stream().map(v -> String.valueOf(v.get(ObjectTreeEnum.PARENT_BID.getCode()))).collect(Collectors.toSet());
            if (l2SfBids.size() != 1) {
                throw new PlmBizException("只能关联同一个二级特性下的四级特性");
            }
            List<MObject> l2Objects = objectModelCrudI.listByBids(new ArrayList<>(l2SfBids), TranscendModel.RSF.getCode());
            Map<String, String> parentBidMap = Stream.of(targetMObjects, l3Objects, l2Objects)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(
                            MBaseData::getBid,
                            v -> v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString(),
                            (v1, v2) -> v1
                    ));
            List<String> l4SfBids = targetMObjects.stream().map(MSpaceAppData::getBid).collect(Collectors.toList());
            List<List<String>> mountSF = l4SfBids.stream().map(v -> {
                List<String> parentBids = new ArrayList<>();
                parentBids.add(v);
                for (int i = 0; i < 3; i++) {
                    if (parentBidMap.containsKey(parentBids.get(0))) {
                        parentBids.add(0, parentBidMap.get(parentBids.get(0)));
                    }
                }
                return parentBids;
            }).collect(Collectors.toList());
            // 删除2级特性和IR原来的关联关系，新增新的关联关系
            QueryWrapper deleteQo2 = new QueryWrapper();
            deleteQo2.eq("target_bid", sourceBid);
            List<QueryWrapper> deleteQueryWrappers2 = QueryWrapper.buildSqlQo(deleteQo2);
            objectModelDomainService.logicalDelete(TranscendModel.RELATION_RSF_IR.getCode(), deleteQueryWrappers2);
            MObject mObject = systemFeatureService.assembleRelationObject(TranscendModel.RELATION_RSF_IR.getCode(), l2SfBids.iterator().next(), sourceBid, addExpandAo.getSpaceBid());
            appDataService.add(TranscendModel.RELATION_RSF_IR.getCode(), mObject);
            //修改实例属性
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.put(SystemFeatureConstant.belong_SF, l2SfBids.iterator().next());
            mSpaceAppData.put(SystemFeatureConstant.MOUNT_SF, mountSF);
            mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(),false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(addExpandAo.getSourceSpaceAppBid(), sourceBid, mSpaceAppData);
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if (addExpandAo != null) {
            return TranscendModel.RELATION_IR_RSF.getCode().equals(addExpandAo.getRelationModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
