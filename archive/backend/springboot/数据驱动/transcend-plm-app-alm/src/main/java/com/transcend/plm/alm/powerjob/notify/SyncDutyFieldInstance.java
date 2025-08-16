package com.transcend.plm.alm.powerjob.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.constant.SysGlobalConst;
import com.transsion.framework.http.util.HttpUtil;
import com.transsion.framework.sdk.core.config.CredentialsProperties;
import com.transsion.framework.uac.service.IUacLoginService;
import com.transsion.sdk.core.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 同步责任田/研发模块实例
 * @author haijun.ren
 */
@Component
@Slf4j
public class SyncDutyFieldInstance implements BasicProcessor {


    @Resource
    private IApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Resource
    private IAppDataService appDataService;

    @Resource
    private IUacLoginService iUacLoginService;

    @Resource
    private CredentialsProperties credentialsProperties;

    @Resource
    private ApmSpaceAppService spaceAppService;

    @Resource
    private CfgObjectFeignClient  cfgObjectClient;

    @Value("${spring.application.sys-code}")
    private String sysCode;

    @Value("${transcend.plm.apm.tonesUrl:https://api-tones.transsion.com/api/code-responsibility-field/owner-field/page-for-pi?page=1&size=1000}")
    private String tonesUrl;

    public static String DUTY_FIELD_MODEL_CODE = "A05";

    public static String DEVELOP_MODULE_MODEL_CODE = "A0I";

    public static String RELATION_MODEL_CODE = "A0R";

    private static String SPACE_BID ="1342178570115805184";

    @Resource
    private ObjectModelStandardI objectModelCrudI;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final String SYNC_DUTY_FIELD_REDIS_KEY = "instance:duty_field:sync_time";



    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        log.info("单机处理器====process=====MonthPersonInputStatistics process");
        Boolean result = syncInstance();
        log.info("单机处理器====process=====MonthPersonInputStatistics process result:{}", result);
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }

    public Boolean syncInstance() {
        //查询本地数据
        List<MObject> oldSourceDataList = listData(DUTY_FIELD_MODEL_CODE);
        List<MObject> oldTargetDataList = listData(DEVELOP_MODULE_MODEL_CODE);
        List<MObject> oldRelationDataList = listData(RELATION_MODEL_CODE);
        Map<String, String> sourceUniqueIdMap = new HashMap<>();
        Map<String, String> targetUniqueIdMap = new HashMap<>();
        if (oldSourceDataList != null && !oldSourceDataList.isEmpty()) {
            for (MObject mObject : oldSourceDataList) {
                sourceUniqueIdMap.put(mObject.get("foreignBid").toString(), mObject.getBid());
            }
        }
        if (oldTargetDataList != null && !oldTargetDataList.isEmpty()) {
            for (MObject mObject : oldTargetDataList) {
                targetUniqueIdMap.put(mObject.get("foreignBid").toString(), mObject.getBid());
            }
        }
        //拉取远程数据
        JSONArray objects = listRemoteData();
        if (CollectionUtils.isEmpty(objects)) {
            return true;
        }
        List<ApmSpaceAppVo> apmSpaceAppVos = spaceAppService.listSpaceAppVoBySpaceBidAndModelCodes(SPACE_BID, Arrays.asList(DUTY_FIELD_MODEL_CODE, DEVELOP_MODULE_MODEL_CODE));
        Map<String, ApmSpaceAppVo> spaceAppVoMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getModelCode, Function.identity(), (k1, k2) -> k1));
        ApmSpaceAppVo sourceSpaceAppVo = spaceAppVoMap.get(DUTY_FIELD_MODEL_CODE);
        ApmSpaceAppVo targetSpaceAppVo = spaceAppVoMap.get(DEVELOP_MODULE_MODEL_CODE);
        List<MObject> addSourceDataList = Lists.newArrayList();
        List<MObject> addTargetDataList = Lists.newArrayList();
        List<MObject> addRelationDataList = Lists.newArrayList();
        List<BatchUpdateBO<MObject>> updateSourceDataList = Lists.newArrayList();
        List<BatchUpdateBO<MObject>> updateTargetDataList = Lists.newArrayList();
        //设置生命周期状态
        ObjectModelLifeCycleVO sourceModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(DUTY_FIELD_MODEL_CODE).getCheckExceptionData();
        ObjectModelLifeCycleVO targetModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(DEVELOP_MODULE_MODEL_CODE).getCheckExceptionData();
        Long tenantId = SsoHelper.getTenantId();
        for (int i = 0; i < objects.size(); i++) {
            String sourceBid = "";
            JSONObject sourceObjectJson = objects.getJSONObject(i);
            if (sourceUniqueIdMap.containsKey(sourceObjectJson.getString("oid"))) {
                updateSourceDataList.add(buildUpdateSourceData(sourceObjectJson, updateSourceDataList));
                sourceBid = sourceUniqueIdMap.get(sourceObjectJson.getString("oid"));
            } else {
                MObject mObject = buildSoueceData(sourceObjectJson, sourceModelLifeCycleVO, sourceSpaceAppVo, tenantId);
                sourceBid = mObject.getBid();
                addSourceDataList.add(mObject);
            }
            if (sourceObjectJson.containsKey("module")) {
                JSONArray module = sourceObjectJson.getJSONArray("module");
                String targetBid = "";
                for (int j = 0; j < module.size(); j++) {
                    JSONObject moduleJson = module.getJSONObject(j);
                    if (targetUniqueIdMap.containsKey(moduleJson.getString("mid"))) {
                        targetBid = targetUniqueIdMap.get(moduleJson.getString("mid"));
                        updateTargetDataList.add(buildUpdateTargetData(moduleJson, updateTargetDataList));
                    } else {
                        MObject mObject = buildTargetData(moduleJson, targetSpaceAppVo, targetModelLifeCycleVO, tenantId);
                        targetBid = mObject.getBid();
                        addTargetDataList.add(mObject);
                    }
                    MObject relationObject = buildRalationData(sourceBid, targetBid, tenantId);
                    addRelationDataList.add(relationObject);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(addSourceDataList)) {
            appDataService.addBatch(DUTY_FIELD_MODEL_CODE,addSourceDataList);
        }
        if (CollectionUtils.isNotEmpty(addTargetDataList)) {
            appDataService.addBatch(DEVELOP_MODULE_MODEL_CODE,addTargetDataList);
        }
        if (CollectionUtils.isNotEmpty(updateSourceDataList)) {
            objectModelCrudI.batchUpdateByQueryWrapper(DUTY_FIELD_MODEL_CODE, updateSourceDataList, false);
        }
        if (CollectionUtils.isNotEmpty(updateTargetDataList)) {
            objectModelCrudI.batchUpdateByQueryWrapper(DEVELOP_MODULE_MODEL_CODE, updateTargetDataList, false);
        }
        if (CollectionUtils.isNotEmpty(addRelationDataList)) {
            if (CollectionUtils.isNotEmpty(oldRelationDataList)) {
                objectModelCrudI.batchLogicalDeleteByBids(RELATION_MODEL_CODE,oldRelationDataList.stream().map(MObject::getBid).collect(Collectors.toList()));
            }
            appDataService.addBatch(RELATION_MODEL_CODE,addRelationDataList);
        }
        return true;
    }

    @NotNull
    private static MObject buildRalationData(String sourceBid, String targetBid, Long tenantId) {
        MObject relationObject = new MObject();
        relationObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
        relationObject.put(RelationEnum.DATA_BID.getCode(), SnowflakeIdWorker.nextIdStr());
        relationObject.put(RelationEnum.SOURCE_BID.getCode(), sourceBid);
        relationObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), sourceBid);
        relationObject.put(RelationEnum.TARGET_BID.getCode(), targetBid);
        relationObject.put(RelationEnum.TARGET_DATA_BID.getCode(), targetBid);
        relationObject.put(RelationEnum.DRAFT.getCode(), Boolean.FALSE);
        relationObject.put(RelationEnum.MODEL_CODE.getCode(), RELATION_MODEL_CODE);
        relationObject.put(RelationEnum.SPACE_BID.getCode(), SPACE_BID);
        relationObject.setTenantId(tenantId);
        return relationObject;
    }

    private List<MObject>  listData(String modelCode) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        QueryCondition queryCondition = new QueryCondition();
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        queryCondition.setQueries(queryWrappers);
        return objectModelCrudI.list(modelCode, queryCondition);
    }

    private MObject buildTargetData(JSONObject moduleJson, ApmSpaceAppVo targetSpaceAppVo, ObjectModelLifeCycleVO targetModelLifeCycleVO, Long tenantId) {
        MObject targetObject = new MObject();
        String targetBid = SnowflakeIdWorker.nextIdStr();
        targetObject.put(TranscendModelBaseFields.BID, targetBid);
        targetObject.put(TranscendModelBaseFields.DATA_BID, targetBid);
        targetObject.put(TranscendModelBaseFields.NAME, moduleJson.getString(TranscendModelBaseFields.NAME));
        targetObject.put("foreignBid", moduleJson.getString("mid"));
        targetObject.put("informationOfRAndDModuleResponsiblePerson", JSONPath.eval(moduleJson, "$.owner.employeeNo"));
        targetObject.setCreatedTime(LocalDateTime.now());
        targetObject.setUpdatedTime(LocalDateTime.now());
        targetObject.put(TranscendModelBaseFields.SPACE_BID, SPACE_BID);
        targetObject.put(TranscendModelBaseFields.MODEL_CODE, DEVELOP_MODULE_MODEL_CODE);
        targetObject.put(TranscendModelBaseFields.SPACE_APP_BID, targetSpaceAppVo.getBid());
        targetObject.put(ObjectEnum.LIFE_CYCLE_CODE.getCode(), targetModelLifeCycleVO.getInitState());
        targetObject.put(TranscendModelBaseFields.LC_MODEL_CODE, targetModelLifeCycleVO.getInitState()+":"+DEVELOP_MODULE_MODEL_CODE);
        targetObject.put(TranscendModelBaseFields.LC_TEMPL_BID, targetModelLifeCycleVO.getLcTemplBid());
        targetObject.put(TranscendModelBaseFields.LC_TEMPL_VERSION, targetModelLifeCycleVO.getLcTemplVersion());
        targetObject.setTenantId(tenantId);
        return targetObject;
    }

    private JSONArray listRemoteData() {
        Map<String, String> headerMap =  getHeaderMap();
        String url = tonesUrl;
        log.info("请求远程接口,Url:{},header:{}", url, JSON.toJSONString(headerMap));
        String result = HttpUtil.get(url, headerMap, null);
        if (StringUtils.isNotBlank(result)) {
            try {
                JSONObject object = JSON.parseObject(result);
                if (!"0000".equals(object.getString("code"))) {
                    throw new PlmBizException("500",object.toJSONString());
                }
                return object.getJSONObject("data").getJSONArray("records");
            }catch (Exception e){
                log.error("解析远程数据失败", e);
            }
        }
        return new JSONArray();
    }

    private Map<String, String> getHeaderMap(){
        Map<String, String> headerMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        headerMap.put(SysGlobalConst.HTTP_HEADER_X_APP_ID, credentialsProperties.getAppKey());
        headerMap.put(SysGlobalConst.HTTP_HEADER_X_AUTH, "r_MDQ3MWVycnVjMmd5Y2VjamJteHdeNDQ1NjU1ODIxOTg0ODA2NjAyMTgxMjQ1");
        headerMap.put(SysGlobalConst.HTTP_HEADER_X_RTOKEN, "u_MjE3MWl1cTcwaWVwamlyaTlwaGFeNDQ1NjU1ODIxNDc1NTUyNjAyMTgxMjQ1");
        return headerMap;
    }

    private MObject buildSoueceData(JSONObject sourceObjectJson, ObjectModelLifeCycleVO sourceModelLifeCycleVO, ApmSpaceAppVo sourceSpaceAppVo, Long tenantId) {
        MObject sourceObject = new MObject();
        String sourceBid = SnowflakeIdWorker.nextIdStr();
        sourceObject.put(TranscendModelBaseFields.BID, sourceBid);
        sourceObject.put(TranscendModelBaseFields.DATA_BID, sourceBid);
        sourceObject.put(TranscendModelBaseFields.NAME, sourceObjectJson.getString(TranscendModelBaseFields.NAME));
        sourceObject.put("foreignBid", sourceObjectJson.getString("oid"));
        sourceObject.put("responsibleFieldOwner", JSONPath.eval(sourceObjectJson, "$.owner.employeeNo"));
        sourceObject.put("deputy", JSONPath.eval(sourceObjectJson, "$.deputy.employeeNo"));
        sourceObject.setCreatedTime(LocalDateTime.now());
        sourceObject.setUpdatedTime(LocalDateTime.now());
        sourceObject.put(TranscendModelBaseFields.SPACE_BID, SPACE_BID);
        sourceObject.put(TranscendModelBaseFields.MODEL_CODE, DUTY_FIELD_MODEL_CODE);
        sourceObject.put(ObjectEnum.LIFE_CYCLE_CODE.getCode(), sourceModelLifeCycleVO.getInitState());
        sourceObject.put(TranscendModelBaseFields.LC_MODEL_CODE,sourceModelLifeCycleVO.getInitState()+":"+DUTY_FIELD_MODEL_CODE);
        sourceObject.put(TranscendModelBaseFields.LC_TEMPL_BID, sourceModelLifeCycleVO.getLcTemplBid());
        sourceObject.put(TranscendModelBaseFields.LC_TEMPL_VERSION, sourceModelLifeCycleVO.getLcTemplVersion());
        sourceObject.put(TranscendModelBaseFields.SPACE_APP_BID, sourceSpaceAppVo.getBid());
        sourceObject.setCreatedBy("18649577");
        sourceObject.setUpdatedBy("18649577");
        sourceObject.setTenantId(tenantId);
        return sourceObject;
    }

    private BatchUpdateBO<MObject> buildUpdateSourceData(JSONObject sourceObjectJson, List<BatchUpdateBO<MObject>> updateSourceDataList) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("foreign_bid");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(sourceObjectJson.getString("oid"));
        MObject updateObject = new MObject();
        updateObject.put(TranscendModelBaseFields.NAME, sourceObjectJson.getString(TranscendModelBaseFields.NAME));
        updateObject.put("personResponsible", JSONPath.eval(sourceObjectJson, "$.owner.employeeNo"));
        updateObject.put("deputy", JSONPath.eval(sourceObjectJson, "$.deputy.employeeNo"));
        BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
        batchUpdateBO.setBaseData(updateObject);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        batchUpdateBO.setWrappers(queryWrapperList);
        return batchUpdateBO;
    }

    private BatchUpdateBO<MObject> buildUpdateTargetData(JSONObject targetObjectJson, List<BatchUpdateBO<MObject>> updateTargetDataList) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("foreign_bid");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(targetObjectJson.getString("mid"));
        MObject updateObject = new MObject();
        updateObject.put(TranscendModelBaseFields.NAME, targetObjectJson.getString(TranscendModelBaseFields.NAME));
        BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
        batchUpdateBO.setBaseData(updateObject);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        batchUpdateBO.setWrappers(queryWrapperList);
        return batchUpdateBO;
    }


}
