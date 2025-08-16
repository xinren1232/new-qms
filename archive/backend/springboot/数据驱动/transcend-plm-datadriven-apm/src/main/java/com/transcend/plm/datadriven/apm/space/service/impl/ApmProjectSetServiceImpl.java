package com.transcend.plm.datadriven.apm.space.service.impl;

import com.google.common.collect.Lists;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.dto.ProjectSetQueryDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ProjectSetDemandViewVo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppTabHeaderService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppTabService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.apm.space.service.IApmProjectSetService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppRelationDataDrivenService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Slf4j
@Service
public class ApmProjectSetServiceImpl implements IApmProjectSetService {

    @Resource
    private IBaseApmSpaceAppRelationDataDrivenService baseApmSpaceAppRelationDataDrivenService;
    @Resource
    private ApmSpaceService apmSpaceService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmSpaceAppTabService apmSpaceAppTabService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private CfgViewFeignClient viewFeignClient;


    @Resource
    private ApmAppTabHeaderService apmAppTabHeaderService;


    /**
     * @param projectSetQueryDto
     * @return {@link PagedResult }<{@link MObject }>
     */
    @Override
    public PagedResult<MObject> getProjectSetPage(ProjectSetQueryDto projectSetQueryDto) {
        //查询关系数据
        RelationMObject relationMObjectProjectSet = RelationMObject.builder().relationModelCode(projectSetQueryDto.getRelationModelCode()).sourceBid(projectSetQueryDto.getSourceInstanceBid()).build();
        List<MObject> projectSetRel = objectModelCrudI.listOnlyRelationMObjects(relationMObjectProjectSet);
        if(CollectionUtils.isEmpty(projectSetRel)){
            return null;
        }
        List<String> targetBids = new ArrayList<>();
        projectSetRel.forEach(mObject -> {
            targetBids.add(mObject.get("targetBid").toString());
        });
        //查询项目集数据
        ModelMixQo modelMixQo = projectSetQueryDto.getPageInfo().getParam();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
        QueryWrapper qo2 = new QueryWrapper();
        qo2.in(TranscendModelBaseFields.BID, targetBids);
        List<QueryWrapper> queryWrappers = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        if(modelMixQo != null){
            queryCondition.setOrders(modelMixQo.getOrders());
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            if(queries == null){
                queries = new ArrayList<>();
            }
            modelMixQo.setQueries(queries);
            queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        }
        if(CollectionUtils.isNotEmpty(queryWrappers)){
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo));
        }else{
            queryWrappers = QueryWrapper.buildSqlQo(qo);
        }
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(projectSetQueryDto.getPageInfo().getCurrent());
        pageQo.setSize(projectSetQueryDto.getPageInfo().getSize());
        PagedResult<MObject> page = objectModelCrudI.page(PROJECT_MODEL_CODE,pageQo, true);
        handleProjectSetDemands(page.getData(),projectSetQueryDto.getModelMixQo());
        return page;
    }

    /**
     * @param mixQoBaseRequest
     * @return {@link PagedResult }<{@link MObject }>
     */
    @Override
    public PagedResult<MObject> getProjectSetDemandPage(BaseRequest<ModelMixQo> mixQoBaseRequest){
        ModelMixQo modelMixQo = mixQoBaseRequest.getParam();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
        //查询有效的空间
        List<String> spaceBids = apmSpaceService.getSpaceBids();
        //查询有效的项目应用
        Set<String> spaceAppBids = apmSpaceAppService.getAllSpaceAppBids(PROJECT_MODEL_CODE);
        if(CollectionUtils.isEmpty(spaceBids)){
            return null;
        }
        QueryWrapper qo2 = new QueryWrapper();
        qo2.in(TranscendModelBaseFields.SPACE_BID, spaceBids);
        QueryWrapper qo3 = new QueryWrapper();
        qo3.in(TranscendModelBaseFields.SPACE_APP_BID, spaceAppBids);
        List<QueryWrapper> queryWrappers = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        if(modelMixQo != null){
            queryCondition.setOrders(modelMixQo.getOrders());
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            if(queries == null){
                queries = new ArrayList<>();
            }
            modelMixQo.setQueries(queries);
            queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        }
        if(CollectionUtils.isNotEmpty(queryWrappers)){
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo));
        }else{
            queryWrappers = QueryWrapper.buildSqlQo(qo);
        }
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo3));
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(mixQoBaseRequest.getCurrent());
        pageQo.setSize(mixQoBaseRequest.getSize());
        PagedResult<MObject> page = objectModelCrudI.page(PROJECT_MODEL_CODE,pageQo, true);
        handleProjectSetDemands(page.getData(),null);
        return page;
    }

    /**
     * @param projectList
     * @param modelMixQo
     * @return {@link List }<{@link MObject }>
     */
    private List<MObject> handleProjectSetDemands(List<MObject> projectList, ModelMixQo modelMixQo){
        //查询所有项目
        Map<String,List<MObjectTree>> mobjectTreeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(projectList)){
            //查询所有空间项目应用的关系tab数据
            List<String> spaceAppBids = projectList.stream().map(e -> e.get(TranscendModelBaseFields.SPACE_APP_BID)+"").distinct().collect(Collectors.toList());
            List<String> spaceBids = projectList.stream().map(e -> e.get(TranscendModelBaseFields.SPACE_BID)+"").distinct().collect(Collectors.toList());
            List<ApmSpaceAppTab> apmSpaceAppTabs = apmSpaceAppTabService.listBySpaceAppBids(spaceAppBids,PROJECT_DEMAND_REL_MODEL_CODE);
            Map<String,ApmSpaceAppTab> apmSpaceAppTabMap = apmSpaceAppTabs.stream().collect(Collectors.toMap(ApmSpaceAppTab::getSpaceAppBid, Function.identity(),(k1,k2)->k1));
            //查询所有空间产品需求的应用
            List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.listBySpaceBidsAndModelCode(spaceBids,DEMAND_MODEL_CODE);
            Map<String,String> spaceBidMap = apmSpaceApps.stream().collect(Collectors.toMap(ApmSpaceApp::getSpaceBid,ApmSpaceApp::getBid,(k1,k2)->k1));
            //查询所有项目的需求
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    15,
                    60,
                    120,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(2048),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
            for(MObject mObject : projectList){
                ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabMap.get(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
                mObject.put("demand_app_bid",spaceBidMap.get(mObject.get(TranscendModelBaseFields.SPACE_BID)+""));
                if(apmSpaceAppTab != null){
                    //查询关系数据
                    if("multiTreeView".equals(apmSpaceAppTab.getViewModelCode()) || "multiTreeAndswimlaneDiagram".equals(apmSpaceAppTab.getViewModelCode())){
                        //查询多对象
                        ApmMultiTreeDto apmMultiTreeDto = new ApmMultiTreeDto();
                        apmMultiTreeDto.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID)+"");
                        apmMultiTreeDto.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
                        apmMultiTreeDto.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
                        apmMultiTreeDto.setTargetSpaceAppBid(spaceBidMap.get(apmMultiTreeDto.getSpaceBid()));
                        apmMultiTreeDto.setSourceBid(mObject.getBid());
                        apmMultiTreeDto.setMultiAppTreeContent(apmSpaceAppTab.getMultiAppTreeContent());
                        apmMultiTreeDto.setFilterUnchecked(true);
                        apmMultiTreeDto.setModelMixQo(ApmMultiTreeModelMixQo.of(modelMixQo));
                        completableFutures.add(CompletableFuture.runAsync(() -> {
                            List<MObjectTree> mObjectTrees = baseApmSpaceAppRelationDataDrivenService.listMultiTree(apmMultiTreeDto, true);
                            mobjectTreeMap.put(mObject.getBid(),mObjectTrees);
                        }, executor));
                    }else {
                        //查询树
                        RelationMObject relationMObject = RelationMObject.builder().build();
                        relationMObject.setSourceBid(mObject.getBid());
                        relationMObject.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
                        relationMObject.setSourceModelCode(PROJECT_MODEL_CODE);
                        relationMObject.setTargetModelCode(DEMAND_MODEL_CODE);
                        relationMObject.setModelMixQo(modelMixQo);
                        completableFutures.add(CompletableFuture.runAsync(() -> {
                            List<MObjectTree> mObjectTrees =  objectModelCrudI.relationTree(relationMObject);
                            mobjectTreeMap.put(mObject.getBid(),mObjectTrees);
                        }, executor));
                    }
                }
            }
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                log.error("项目集产品需求明细多线程查询异常", ex);
                throw new TranscendBizException("项目集产品需求明细多线程查询异常");
            }).join();
            for(MObject mObject : projectList){
                mObject.put("mObjectTrees",mobjectTreeMap.get(mObject.getBid()));
            }
        }
        return projectList;
    }

    /**
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listProjectSetDemands(){
        //查询所有项目
        QueryWrapper qo = new QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
        List<QueryWrapper> wrappers =  QueryWrapper.buildSqlQo(qo);
        List<MObject> projectList = objectModelCrudI.list(PROJECT_MODEL_CODE,wrappers);
        List<MObject> resList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(projectList)){
            //查询所有空间项目应用的关系tab数据
            List<String> spaceAppBids = projectList.stream().map(e -> e.get(TranscendModelBaseFields.SPACE_APP_BID)+"").collect(Collectors.toList());
            List<String> spaceBids = projectList.stream().map(e -> e.get(TranscendModelBaseFields.SPACE_BID)+"").collect(Collectors.toList());
            List<ApmSpaceAppTab> apmSpaceAppTabs = apmSpaceAppTabService.listBySpaceAppBids(spaceAppBids,PROJECT_DEMAND_REL_MODEL_CODE);
            Map<String,ApmSpaceAppTab> apmSpaceAppTabMap = apmSpaceAppTabs.stream().collect(Collectors.toMap(ApmSpaceAppTab::getSpaceAppBid, Function.identity()));
            //查询所有空间产品需求的应用
            List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.listBySpaceBidsAndModelCode(spaceBids,DEMAND_MODEL_CODE);
            Map<String,String> spaceBidMap = apmSpaceApps.stream().collect(Collectors.toMap(ApmSpaceApp::getSpaceBid,ApmSpaceApp::getBid));
            //查询所有项目的需求
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    15,
                    60,
                    120,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(2048),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
            for(MObject mObject : projectList){
                ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabMap.get(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
                if(apmSpaceAppTab != null){
                    //查询关系数据
                    if("multiTreeView".equals(apmSpaceAppTab.getViewModelCode()) || "multiTreeAndswimlaneDiagram".equals(apmSpaceAppTab.getViewModelCode())){
                        //查询多对象
                        ApmMultiTreeDto apmMultiTreeDto = new ApmMultiTreeDto();
                        apmMultiTreeDto.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID)+"");
                        apmMultiTreeDto.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
                        apmMultiTreeDto.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
                        apmMultiTreeDto.setTargetSpaceAppBid(spaceBidMap.get(apmMultiTreeDto.getSpaceBid()));
                        apmMultiTreeDto.setSourceBid(mObject.getBid());
                        apmMultiTreeDto.setModelMixQo(ApmMultiTreeModelMixQo.of(apmSpaceAppTab.getConfigContent()));
                        apmMultiTreeDto.setMultiAppTreeContent(apmSpaceAppTab.getMultiAppTreeContent());
                        apmMultiTreeDto.setFilterUnchecked(true);
                        completableFutures.add(CompletableFuture.runAsync(() -> {
                            List<MObjectTree> mObjectTrees = baseApmSpaceAppRelationDataDrivenService.listMultiTree(apmMultiTreeDto, true);
                            mObject.put("mObjectTrees",mObjectTrees);
                            resList.add(mObject);
                        }, executor));
                    }else {
                        //查询树
                        RelationMObject relationMObject = RelationMObject.builder().build();
                        relationMObject.setSourceBid(mObject.getBid());
                        relationMObject.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
                        relationMObject.setSourceModelCode(PROJECT_MODEL_CODE);
                        relationMObject.setTargetModelCode(DEMAND_MODEL_CODE);
                        completableFutures.add(CompletableFuture.runAsync(() -> {
                            List<MObjectTree> mObjectTrees =  objectModelCrudI.relationTree(relationMObject);
                            mObject.put("mObjectTrees",mObjectTrees);
                            resList.add(mObject);
                        }, executor));
                    }
                }
            }
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                log.error("项目集产品需求明细多线程查询异常", ex);
                throw new TranscendBizException("项目集产品需求明细多线程查询异常");
            }).join();
        }
        return resList;
    }

    /**
     * @return {@link Map }<{@link String }, {@link ProjectSetDemandViewVo }>
     */
    @Override
    public Map<String, ProjectSetDemandViewVo> getAllDemandBaseViews(){
        Map<String, ProjectSetDemandViewVo> result = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Set<String> spaceAppBids = apmSpaceAppService.getAllSpaceAppBids(DEMAND_MODEL_CODE);
        Map<String, CfgViewVo> viewVoMap = viewFeignClient.getByBids(spaceAppBids).getCheckExceptionData();
        List<ApmAppTabHeader> apmAppTabHeaders = apmAppTabHeaderService.getApmAppTabHeaders(spaceAppBids);
        Map<String,ApmAppTabHeader> apmAppTabHeaderMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmAppTabHeaders)){
            apmAppTabHeaderMap = apmAppTabHeaders.stream().collect(Collectors.toMap(ApmAppTabHeader::getBizBid, Function.identity()));
        }
        for (Map.Entry<String, CfgViewVo> entry : viewVoMap.entrySet()) {
            ProjectSetDemandViewVo projectSetDemandViewVo = new ProjectSetDemandViewVo();
            projectSetDemandViewVo.setCfgViewVo(entry.getValue());
            projectSetDemandViewVo.setApmAppTabHeader(apmAppTabHeaderMap.get(entry.getKey()));
            projectSetDemandViewVo.setSpaceAppBid(entry.getKey());
            result.put(entry.getKey(),projectSetDemandViewVo);
        }
        return result;
    }

    /**
     * @param mObject
     * @return {@link List }<{@link MObjectTree }>
     */
    @Override
    public List<MObjectTree> getProjectDemandDetail(MObject mObject) {
        List<String> spaceAppBids = new ArrayList<>();
        spaceAppBids.add(mObject.get(TranscendModelBaseFields.SPACE_APP_BID) + "");
        List<String> spaceBids = new ArrayList<>();
        spaceBids.add(mObject.get(TranscendModelBaseFields.SPACE_BID) + "");
        //查询所有空间项目应用的关系tab数据
        List<ApmSpaceAppTab> apmSpaceAppTabs = apmSpaceAppTabService.listBySpaceAppBids(spaceAppBids, PROJECT_DEMAND_REL_MODEL_CODE);
        Map<String, ApmSpaceAppTab> apmSpaceAppTabMap = apmSpaceAppTabs.stream().collect(Collectors.toMap(ApmSpaceAppTab::getSpaceAppBid, Function.identity()));
        //查询所有空间产品需求的应用
        List<ApmSpaceApp> apmSpaceApps = apmSpaceAppService.listBySpaceBidsAndModelCode(spaceBids, DEMAND_MODEL_CODE);
        Map<String, String> spaceBidMap = apmSpaceApps.stream().collect(Collectors.toMap(ApmSpaceApp::getSpaceBid, ApmSpaceApp::getBid));
        ApmSpaceAppTab apmSpaceAppTab = apmSpaceAppTabMap.get(mObject.get(TranscendModelBaseFields.SPACE_APP_BID) + "");
        if ("multiTreeView".equals(apmSpaceAppTab.getViewModelCode()) || "multiTreeAndswimlaneDiagram".equals(apmSpaceAppTab.getViewModelCode())) {
            ApmMultiTreeDto apmMultiTreeDto = new ApmMultiTreeDto();
            apmMultiTreeDto.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID)+"");
            apmMultiTreeDto.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
            apmMultiTreeDto.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
            apmMultiTreeDto.setTargetSpaceAppBid(spaceBidMap.get(apmMultiTreeDto.getSpaceBid()));
            apmMultiTreeDto.setSourceBid(mObject.getBid());
            apmMultiTreeDto.setModelMixQo(ApmMultiTreeModelMixQo.of(apmSpaceAppTab.getConfigContent()));
            apmMultiTreeDto.setMultiAppTreeContent(apmSpaceAppTab.getMultiAppTreeContent());
            apmMultiTreeDto.setFilterUnchecked(true);
            return baseApmSpaceAppRelationDataDrivenService.listMultiTree(apmMultiTreeDto, true);
        }else {
            RelationMObject relationObject = RelationMObject.builder().build();
            relationObject.setSourceBid(mObject.getBid());
            relationObject.setRelationModelCode(PROJECT_DEMAND_REL_MODEL_CODE);
            relationObject.setSourceModelCode(PROJECT_MODEL_CODE);
            relationObject.setTargetModelCode(DEMAND_MODEL_CODE);
            return (List<MObjectTree>) objectModelCrudI.relationTree(relationObject);
        }
    }

    /**
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listFristProjectSetDemands(){
        //查询所有项目
        QueryWrapper qo = new QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
        List<QueryWrapper> wrappers =  QueryWrapper.buildSqlQo(qo);
        List<MObject> projectList = objectModelCrudI.list(PROJECT_MODEL_CODE,wrappers);
        List<MObject> resList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(projectList)){
            MObject object = projectList.get(0);
            List<MObjectTree> mObjectTrees = getProjectDemandDetail(object);
            object.put("mObjectTrees",mObjectTrees);
        }
        return projectList;
    }
}
