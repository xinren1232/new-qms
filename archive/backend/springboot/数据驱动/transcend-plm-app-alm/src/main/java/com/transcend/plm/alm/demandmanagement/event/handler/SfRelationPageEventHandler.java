package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.entity.RelationTreeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationTreeEventHandler;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.BaseObjectTreeService;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ir挂载特性查询处理
 * @author qing.chen
 * @date 2025/01/13 14:37
 **/
@Slf4j
@Component
public class SfRelationPageEventHandler extends AbstractRelationTreeEventHandler {

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private BaseObjectTreeService baseObjectTreeService;

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 后置
     *
     * @param param  入参
     * @param result 返回结果
     * @return 返回结果
     */
    @Override
    public List<MObjectTree> postHandle(RelationTreeEventHandlerParam param, List<MObjectTree> result) {
        //ir关联的是4级特性，需要补全1-4级特性
        if (CollectionUtils.isEmpty(result)) {
            //如果没有关联四级特性，但是关联了二级特性，则补全二级特性
            QueryCondition queryCondition = new QueryCondition();
            QueryWrapper qo = new QueryWrapper();
            qo.eq(RelationEnum.TARGET_BID.getColumn(), param.getRelationMObject().getSourceBid());
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            queryCondition.setQueries(queryWrappers);
            List<MObject> mRelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_RSF_IR.getCode(), queryCondition);
            if (CollectionUtils.isEmpty(mRelationObjectList)){
                return super.postHandle(param, result);
            }
            List<Object> sourceBids = mRelationObjectList.stream().map(v -> v.get(RelationEnum.SOURCE_BID.getCode())).collect(Collectors.toList());
            List<MObject> mObjectList = objectModelCrudI.listByBids(sourceBids, TranscendModel.RSF.getCode());
            if (CollectionUtils.isEmpty(mObjectList)){
                return super.postHandle(param, result);
            }
            //查询父节点
            List<String> parentBids = mObjectList.stream().map(v->v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString()).collect(Collectors.toList());
            List<MObject> parentObjectList = objectModelCrudI.listByBids(parentBids, TranscendModel.RSF.getCode());
            if (CollectionUtils.isNotEmpty(parentObjectList)){
                mObjectList.addAll(parentObjectList);
            }
            List<MObjectTree> systemFeatureTree = mObjectList.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.setChecked(true);
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            result = baseObjectTreeService.convert2Tree(systemFeatureTree);
            return super.postHandle(param, result);
        }
        List<String> parentBids = result.stream().map(MObjectTree::getParentBid).collect(Collectors.toList());
        List<String> bids = result.stream().map(MObjectTree::getBid).collect(Collectors.toList());
        for (int i = 0; i < 3; i++) {
            //查询子节点
            QueryCondition queryCondition = new QueryCondition();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.in(ObjectTreeEnum.PARENT_BID.getCode(), bids);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
            queryCondition.setQueries(queryWrappers);
            List<MObject> mObjectList = objectModelDomainService.list(TranscendModel.RSF.getCode(), queryCondition);
            if (CollectionUtils.isEmpty(mObjectList)){
                break;
            }
            List<MObjectTree> systemFeatureTree = mObjectList.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            result.addAll(systemFeatureTree);
            bids = mObjectList.stream().map(MObject::getBid).collect(Collectors.toList());
        }
        for (int i = 0; i < 3; i++) {
            //查询父节点
            List<MObject> mObjectList = objectModelCrudI.listByBids(parentBids, TranscendModel.RSF.getCode());
            if (CollectionUtils.isEmpty(mObjectList)){
                break;
            }
            List<MObjectTree> systemFeatureTree = mObjectList.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            result.addAll(systemFeatureTree);
            parentBids = systemFeatureTree.stream().map(MObjectTree::getParentBid).collect(Collectors.toList());
        }
        result = baseObjectTreeService.convert2Tree(result);
        //全部标记为选中
        setChecked(result);
        return result;
    }

    private void setChecked(List<MObjectTree> result) {
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(v->{
                v.setChecked(true);
                setChecked(v.getChildren());
            });
        }
    }

    /**
     * 匹配是否执行
     *
     * @param param 入参
     * @return true:匹配上需要执行; false 匹配不上不需要执行
     */
    @Override
    public boolean isMatch(RelationTreeEventHandlerParam param) {
        return TranscendModel.RELATION_IR_RSF.getCode().equals(param.getRelationMObject().getRelationModelCode());
    }
}
