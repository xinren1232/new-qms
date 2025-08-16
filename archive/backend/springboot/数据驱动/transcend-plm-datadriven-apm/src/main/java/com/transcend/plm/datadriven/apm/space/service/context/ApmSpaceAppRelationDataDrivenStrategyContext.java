package com.transcend.plm.datadriven.apm.space.service.context;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.GroupListResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.relation.delete.StructureRelDel;
import com.transcend.plm.datadriven.api.model.relation.qo.QueryPathQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationParentQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationTreeQo;
import com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo;
import com.transcend.plm.datadriven.apm.event.annotation.TranscendEvent;
import com.transcend.plm.datadriven.apm.event.enums.EventHandlerTypeEnum;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppRelationAddParam;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppRelationDataDrivenService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对象数据驱动策略上下文
 * @author yinbin
 * @version:
 * @date 2023/12/18 17:04
 */
@Component
@Primary
public class ApmSpaceAppRelationDataDrivenStrategyContext implements IBaseApmSpaceAppRelationDataDrivenService {
    public static final String NORMAL = "NORMAL";
    public static final String VERSION = "VERSION";

    public static final String STRATEGY_NAME = "AppRelationDataDrivenService";

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private Map<String, IBaseApmSpaceAppRelationDataDrivenService> strategyMap;

    @Resource
    private IPermissionCheckService permissionCheckService;


    /**
     * 获取数据驱动策略
     * @param spaceAppBid 空间应用bid
     * @return
     */
    private IBaseApmSpaceAppRelationDataDrivenService getDataDrivenStrategy(String spaceAppBid){
        if (StringUtils.isBlank(spaceAppBid)) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp == null) {
            return strategyMap.get(NORMAL + STRATEGY_NAME);
        }
        return strategyMap.get((Boolean.TRUE.equals(apmSpaceApp.getIsVersionObject()) ? VERSION : NORMAL) + STRATEGY_NAME);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_BATCH_DELETE)
    public Boolean batchDelete(String spaceBid, RelationDelAndRemParamAo relationDelAndRemParamAo) {
        return getDataDrivenStrategy(relationDelAndRemParamAo.getSourceSpaceAppBid()).batchDelete(spaceBid, relationDelAndRemParamAo);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_BATCH_REMOVE)
    public Boolean batchRemove(String spaceBid, RelationDelAndRemParamAo delAndRemParamAo) {
        //检查源对象是否有编辑权限
        PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
        permissionCheckDto.setSpaceBid(spaceBid);
        permissionCheckDto.setSpaceAppBid(delAndRemParamAo.getSourceSpaceAppBid());
        permissionCheckDto.setInstanceBid(delAndRemParamAo.getSourceBid());
        permissionCheckDto.setOperatorCode(OperatorEnum.EDIT.getCode());
        boolean hasPermission = permissionCheckService.checkInstancePermission(permissionCheckDto);
        if(!hasPermission){
            throw new BusinessException("源对象没有编辑权限");
        }
        return getDataDrivenStrategy(delAndRemParamAo.getSourceSpaceAppBid()).batchRemove(spaceBid, delAndRemParamAo);
    }

    @Override
    public Boolean removeAllRelation(SpaceAppRelationAddParam spaceAppRelationAddParam) {
        return null;
    }

    @Override
    public Boolean moveTreeNode(String spaceBid, String spaceAppBid, String targetSpaceAppBid, String sourceBid, List<MoveTreeNodeParam> moveTreeNodeParams) {
        return getDataDrivenStrategy(spaceAppBid).moveTreeNode(spaceBid, spaceAppBid, targetSpaceAppBid, sourceBid, moveTreeNodeParams);
    }

    @Override
    public Boolean add(SpaceAppRelationAddParam spaceAppRelationAddParam) {
        return getDataDrivenStrategy(spaceAppRelationAddParam.getSpaceAppBid()).add(spaceAppRelationAddParam);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_BATCH_SELECT)
    public Boolean multiBatchSelect(String spaceBid, String relationSpaceAppBid, ApmRelationMultiTreeAddParam apmRelationMultiTreeAddParam) {
        return getDataDrivenStrategy(relationSpaceAppBid).multiBatchSelect(spaceBid, relationSpaceAppBid, apmRelationMultiTreeAddParam);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_ADD)
    public Object addExpand(String spaceBid, String spaceAppBid, String source, AddExpandAo addExpandAo) {
        return getDataDrivenStrategy(spaceAppBid).addExpand(spaceBid, spaceAppBid, source, addExpandAo);
    }

    @Override
    public List<MObjectTree> listMultiTree(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText) {
        return getDataDrivenStrategy(apmMultiTreeDto.getTargetSpaceAppBid()).listMultiTree(apmMultiTreeDto, filterRichText);
    }

    @Override
    public List<MObjectTree> listMultiTreeGroupBy(ApmMultiTreeDto apmMultiTreeDto) {
        return getDataDrivenStrategy(apmMultiTreeDto.getTargetSpaceAppBid()).listMultiTreeGroupBy(apmMultiTreeDto);
    }

    @Override
    public GroupListResult<MSpaceAppData> listMultiTreeGroupKb(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText) {
        return getDataDrivenStrategy(apmMultiTreeDto.getTargetSpaceAppBid()).listMultiTreeGroupKb(apmMultiTreeDto, filterRichText);

    }

    @Override
    public GroupListResult<MSpaceAppData> groupList(String groupProperty, RelationMObject relationMObject) {
        return getDataDrivenStrategy(null).groupList(groupProperty, relationMObject);
    }

    @Override
    public boolean copyMObject(MObjectCopyAo mObjectCopyAo) {
        return getDataDrivenStrategy(null).copyMObject(mObjectCopyAo);
    }

    @Override
    public void exportExcel(String spaceAppBid, String type, RelationMObject relationMObject, HttpServletResponse response) {
        getDataDrivenStrategy(spaceAppBid).exportExcel(spaceAppBid, type, relationMObject, response);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_LIST)
    public List<MObject> listRelation(String spaceBid, String spaceAppBid, RelationMObject relationMObject) {
        return getDataDrivenStrategy(spaceAppBid).listRelation(spaceBid, spaceAppBid, relationMObject);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_PAGE)
    public PagedResult<MObject> listRelationPage(String spaceBid, String spaceAppBid, BaseRequest<RelationMObject> relationMObject, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).listRelationPage(spaceBid, spaceAppBid, relationMObject, filterRichText);
    }

    @Override
    @TranscendEvent(eventHandlerType = EventHandlerTypeEnum.RELATION_TREE)
    public List<MObjectTree> tree(String spaceBid, String spaceAppBid, RelationMObject relationMObject, boolean filterRichText) {
        return getDataDrivenStrategy(spaceAppBid).tree(spaceBid, spaceAppBid, relationMObject, filterRichText);
    }

    @Override
    public List<MObjectTree> listRelationTree(String spaceBid, String spaceAppBid, RelationTreeQo qo) {
        return getDataDrivenStrategy(spaceAppBid).listRelationTree(spaceBid, spaceAppBid, qo);
    }

    @Override
    public List<MObject> listRelParent(String spaceBid, String spaceAppBid, RelationParentQo qo) {
        return getDataDrivenStrategy(spaceAppBid).listRelParent(spaceBid, spaceAppBid, qo);
    }

    @Override
    public boolean structureBatchRemoveRel(String spaceBid, String spaceAppBid, StructureRelDel structureRelDel) {
        return getDataDrivenStrategy(spaceAppBid).structureBatchRemoveRel(spaceBid, spaceAppBid, structureRelDel);
    }

    @Override
    public List<QueryPathVo> queryCadPath(String spaceBid, String spaceAppBid, QueryPathQo queryPathQo) {
        return getDataDrivenStrategy(spaceAppBid).queryCadPath(spaceBid, spaceAppBid, queryPathQo);
    }
}
