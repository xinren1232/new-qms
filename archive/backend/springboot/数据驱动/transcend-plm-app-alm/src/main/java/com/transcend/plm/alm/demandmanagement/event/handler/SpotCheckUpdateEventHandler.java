package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.qo.BatchRemoveRelationQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppRelationDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author unknown
 * IR状态变更需要驱动SR状态变更
 */
@Slf4j
@Component
public class SpotCheckUpdateEventHandler extends AbstractUpdateEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IBaseApmSpaceAppRelationDataDrivenService baseApmSpaceAppRelationDataDrivenService;

    @Resource
    private IBaseApmSpaceAppDataDrivenService baseApmSpaceAppDataDrivenService;


    /**
     * 认证空间
     */
    @Value("${transcend.plm.apm.moudle.authSpaceBid:1281300234915565568}")
    private String authSpaceBid;

    /**
     * 认证任务
     */
    @Value("${transcend.plm.apm.moudle.authTaskAppBid:1281313877128400896}")
    private String authTaskAppBid;

    /**
     * 认证任务
     */
    @Value("${transcend.plm.apm.moudle.authSpotCheckAppBid:1286327117135093760}")
    private String authSpotCheckAppBid;

    /**
     * 项目认证点检小结  modelCode
     */
    @Value("${transcend.plm.apm.moudle.sourceModelCode:A7N}")
    private String sourceModelCode;
    /**
     * 项目认证  modelCode
     */
    @Value("${transcend.plm.apm.moudle.targetModelCode:A6ZA00}")
    private String targetModelCode;
    /**
     * 点检小结-项目认证任务  modelCode
     */
    @Value("${transcend.plm.apm.moudle.relationModelCode:A7O}")
    private String relationModelCode;

    private static final String source = "inner";

    public static final String NODE_REDIS_KEY = "node_check_redis_key";

    private static final List<Object> spotCheckPointList = Arrays.asList("QR0", "QR1", "QR2", "MQR");

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {

        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        MSpaceAppData paramMSpaceAppData = param.getMSpaceAppData();
        if (authSpaceBid.equals(param.getSpaceBid())) {
            Map<String, Object> mapObj = new HashMap<>();
            String paramBid = param.getBid();
            String modelCode = apmSpaceApp.getModelCode();
            //===通过完成时间去改
            String expectedCompleteTimeStr ="";
            if(null!=paramMSpaceAppData.get("expectedCompleteTime") && paramMSpaceAppData.get("expectedCompleteTime") instanceof  LocalDate){
                LocalDate expectedCompleteTime = (LocalDate)paramMSpaceAppData.get("expectedCompleteTime");
                expectedCompleteTimeStr=expectedCompleteTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }else{
                expectedCompleteTimeStr = (String) paramMSpaceAppData.getOrDefault("expectedCompleteTime","");
            }
            //修改了认证状态字段后同步修改超期状态
            if (StringUtils.isNotEmpty(expectedCompleteTimeStr) && StringUtils.isNotEmpty(modelCode) && targetModelCode.equals(modelCode)) {
                syncChangeOverDateStatus(expectedCompleteTimeStr,paramBid,mapObj);
            }
            //通过生命周期去改
            String lifeCycLeCode = (String) paramMSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode());
            if (StringUtils.isNotEmpty(lifeCycLeCode) && StringUtils.isNotEmpty(modelCode) && targetModelCode.equals(modelCode)) {
                syncChangeOverDateStatusLifeCode(lifeCycLeCode,paramBid,mapObj);
            }

            String expectedDeliveryTime = (String) paramMSpaceAppData.get("expectedDeliveryTime");
            if (StringUtils.isNotEmpty(expectedDeliveryTime) && StringUtils.isNotEmpty(modelCode) && targetModelCode.equals(modelCode)) {
                String isClc = "自定义";

                QueryWrapper queryTargetWrapper = new QueryWrapper();
                queryTargetWrapper.eq(RelationEnum.BID.getColumn(), paramBid);
                List<QueryWrapper> queryTarget = QueryWrapper.buildSqlQo(queryTargetWrapper);
                List<MObject> targetList = objectModelCrudI.list(targetModelCode, queryTarget);

                if (CollectionUtils.isNotEmpty(targetList)) {
                    // 预计完成时间计算方式
                    String clcMethod = (String) targetList.get(0).get("clcMthExpectedCompletionTime");
                    String lifeCycleCode = (String) targetList.get(0).get("lifeCycleCode");
                    if (StringUtils.isNotEmpty(clcMethod) && !clcMethod.equals(isClc)) {
                        String fixedCycleStr = (String) targetList.get(0).get("fixedCycle");
                        if (StringUtils.isNotEmpty(fixedCycleStr)) {
                            int fixedCycle = Integer.parseInt(fixedCycleStr);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate localDate = LocalDate.parse(expectedDeliveryTime, formatter);
                            LocalDate date = localDate.plusDays(fixedCycle);
                            MSpaceAppData mSpaceAppData = new MSpaceAppData();
                            mSpaceAppData.setBid(paramBid);
                            mSpaceAppData.put("expectedCompleteTime", date);
                            Boolean b = baseApmSpaceAppDataDrivenService.updatePartialContent(authTaskAppBid, paramBid, mSpaceAppData);
                            MSpaceAppData mSpaceAppData2 = new MSpaceAppData();
                            mSpaceAppData2.setBid(paramBid);
                            LocalDateTime expectedCompleteTime = LocalDateTime.of(LocalDate.parse(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MAX);
                           log.info("预计发货时间expectedCompleteTime{}currentTime{}",expectedCompleteTime,LocalDateTime.now());
                            if(LocalDateTime.now().isAfter(expectedCompleteTime)){
                                if(!Arrays.asList("NOT_INVOLVED", "CANCELLED", "PAUSE", "CERTIFIED","CERTIFIED_REUSE").contains(lifeCycleCode)){
                                    mSpaceAppData2.put("isOverDate", "是");
                                    mapObj.put("isOverDate", "是");
                                }
                            }else{
                                mSpaceAppData2.put("isOverDate", "否");
                                mapObj.put("isOverDate", "否");
                            }
                            baseApmSpaceAppDataDrivenService.updatePartialContent(authTaskAppBid, paramBid, mSpaceAppData2);

                            String key = NODE_REDIS_KEY + paramBid;
                            if (b) {
                                mapObj.put("__UPDATE_INSTANCE__", Boolean.TRUE);
                                mapObj.put("expectedCompleteTime", date);
                            } else {
                                mapObj.put("__UPDATE_INSTANCE__", Boolean.FALSE);
                            }

                            redisTemplate.opsForValue().set(key, JSON.toJSONString(mapObj));
                            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
                        }
                    }
                }
            }

            String checkPoint = (String) paramMSpaceAppData.get("checkPoint");
            if (StringUtils.isNotEmpty(checkPoint) && spotCheckPointList.contains(checkPoint)) {
                updateNodeRelation(paramBid, checkPoint);

            }

//            https://pfgatewayuat.transsion.com:9199/transcend-plm-datadriven-apm/apm/space/1281300234915565568/app/1281313877128400896/data-driven/1296050191681990656/instanceEvent/afterChange
        }

        return super.postHandle(param, result);
    }

    private void syncChangeOverDateStatusLifeCode(String  lifeCycleCode, String paramBid,Map<String, Object> mapObj) {
        //ipm2.6 3.19.17当修改认证状态为 已出证，已取消，暂停，不涉及的时候，同步修改是否超期为否
        try {
            List<String> list = Arrays.asList("NOT_INVOLVED", "CANCELLED", "PAUSE", "CERTIFIED","CERTIFIED_REUSE");
            List<String> bids =new ArrayList<>();
            bids.add(paramBid);
            if (StringUtils.isNotEmpty(lifeCycleCode) && list.contains(lifeCycleCode)) {
                //如果状态为这几个之一;
                MObject updateObject = new MObject();
                updateObject.put("isOverDate","否");
                Boolean b =objectModelCrudI.batchUpdatePartialContentByIds(targetModelCode, updateObject, bids);
                if(b){
                    mapObj.put("isOverDate","否");
                    mapObj.put("__UPDATE_INSTANCE__", Boolean.TRUE);
                }
                String key = NODE_REDIS_KEY + paramBid;
                redisTemplate.opsForValue().set(key, JSON.toJSONString(mapObj));
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("修改｛｝是否超期状态失败{}",paramBid,e);
            throw new RuntimeException(e);
        }
    }

    private void syncChangeOverDateStatus(String  expectedCompleteTimeStr, String paramBid,Map<String, Object> mapObj) {
        //ipm2.6 3.19.17当修改认证状态为 已出证，已取消，暂停，不涉及的时候，同步修改是否超期为否
        MObject data = objectModelCrudI.getByBid(targetModelCode, paramBid);
        //获取要修改的数据的生命周期
        String  lifeCycleCode="";
        if (data != null) {
            lifeCycleCode = (String) data.get("lifeCycleCode");
        }
        try {
            List<String> bids =new ArrayList<>();
            bids.add(paramBid);
            if(StringUtils.isNotEmpty(expectedCompleteTimeStr)){
                LocalDateTime expectedCompleteTime = LocalDateTime.of(LocalDate.parse(expectedCompleteTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MAX);
                MObject updateObject = new MObject();
                //判断生命周期状态
                log.info("修改认证状态判断+expectedCompleteTime:{}，当前时间{}", expectedCompleteTime,LocalDateTime.now());
                if(expectedCompleteTime.isAfter(LocalDateTime.now())){
                    updateObject.put("isOverDate","否");
                    mapObj.put("isOverDate","否");
                }else{
                    if(!Arrays.asList("NOT_INVOLVED", "CANCELLED", "PAUSE", "CERTIFIED","CERTIFIED_REUSE").contains(lifeCycleCode)){
                        updateObject.put("isOverDate","是");
                        mapObj.put("isOverDate", "是");
                        log.info("设置为超期");
                    }else{
                        updateObject.put("isOverDate","否");
                        mapObj.put("isOverDate","否");
                    }
                }
                Boolean b = objectModelCrudI.batchUpdatePartialContentByIds(targetModelCode, updateObject, bids);
                if(b){
                    mapObj.put("__UPDATE_INSTANCE__", Boolean.TRUE);
                }else{
                    mapObj.put("__UPDATE_INSTANCE__", Boolean.FALSE);
                }
                String key = NODE_REDIS_KEY + paramBid;
                redisTemplate.opsForValue().set(key, JSON.toJSONString(mapObj));
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("修改状态｛｝是否超期状态失败{}",paramBid,e);
            throw new RuntimeException(e);
        }
    }

    private void updateNodeRelation(String paramTargetBid, String checkPoint) {
        QueryWrapper queryTargetWrapper = new QueryWrapper();
        queryTargetWrapper.eq(RelationEnum.BID.getColumn(), paramTargetBid);
        List<QueryWrapper> queryTarget = QueryWrapper.buildSqlQo(queryTargetWrapper);
        List<MObject> targetList = objectModelCrudI.list(targetModelCode, queryTarget);
        if (CollectionUtil.isNotEmpty(targetList)) {
            String projectBid = (String) targetList.get(0).get("belongProjectBid");

            List<MObject> transcendTaskDataList = transcendTaskQuery(projectBid);
            List<MObject> transcendPointCheckDataList = transcendPointCheckQuery(projectBid);

            List<String> targetBids = Lists.newArrayList();

            if (CollectionUtils.isNotEmpty(transcendTaskDataList) || CollectionUtil.isNotEmpty(transcendPointCheckDataList)) {
                for (Map<String, Object> mapObj : transcendTaskDataList) {
                    String targetBid = (String) mapObj.get("bid");
                    targetBids.add(targetBid);
                }

                List<String> sourceBids = Lists.newArrayList();
                for (Map<String, Object> mapObj : transcendPointCheckDataList) {
                    String relationBid = (String) mapObj.get("bid");
                    sourceBids.add(relationBid);
                }

                BatchRemoveRelationQo batchRemoveRelationQo = new BatchRemoveRelationQo();
                batchRemoveRelationQo.setRelationModelCode(relationModelCode);
                batchRemoveRelationQo.setSourceBids(sourceBids);
                batchRemoveRelationQo.setTargetBids(targetBids);

                Boolean b = baseApmSpaceAppDataDrivenService.batchRemoveRelation(authSpotCheckAppBid, batchRemoveRelationQo);

                saveNodeCheckInterlinkData(transcendPointCheckDataList, transcendTaskDataList);

                Map<String, Object> mapObj = new HashMap<>();
                String key = NODE_REDIS_KEY + paramTargetBid;
                if (b) {
                    mapObj.put("__UPDATE_LIST__", Boolean.TRUE);
                    mapObj.put("checkPoint", checkPoint);
                } else {
                    mapObj.put("__UPDATE_LIST__", Boolean.FALSE);
                }

                redisTemplate.opsForValue().set(key, JSON.toJSONString(mapObj));
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);

            }
        }
    }

    private void saveNodeCheckInterlinkData(List<MObject> linkedHashMapList, List<MObject> savedTaskDataList) {
        for (Map<String, Object> map : linkedHashMapList) {
            AddExpandAo addExpandAo = new AddExpandAo();
            // 项目认证点检小结
            String sourceBid = (String) map.get("bid");
            String sourceDataBid = (String) map.get("dataBid");
            addExpandAo.setSpaceBid(authSpaceBid);
            addExpandAo.setSpaceAppBid(authTaskAppBid);
            addExpandAo.setSourceBid(sourceBid);
            addExpandAo.setSourceDataBid(sourceDataBid);
            addExpandAo.setSourceSpaceAppBid(authSpotCheckAppBid);

            // 点检小结-项目认证任务
            addExpandAo.setRelationModelCode(relationModelCode);
            // 认证任务
            addExpandAo.setTargetModelCode(targetModelCode);
//            addExpandAo.setViewModelCode(tableViewCode);
            List<MSpaceAppData> targetMObjects = null;

            String nodeCheckPoint = (String) map.get("checkPoint");
            // "QR0", "QR1", "QR2", "MQR"
            switch (nodeCheckPoint) {
                case "QR0":
                    List<String> qr0NodeList = new ArrayList<>();
                    qr0NodeList.add("QR0");
                    targetMObjects = getTargetMObjects(qr0NodeList, savedTaskDataList);
                    break;
                case "QR1":
                    List<String> qr1NodeList = new ArrayList<>();
                    qr1NodeList.add("QR0");
                    qr1NodeList.add("QR1");
                    targetMObjects = getTargetMObjects(qr1NodeList, savedTaskDataList);
                    break;
                case "QR2":
                    List<String> qr2NodeList = new ArrayList<>();
                    qr2NodeList.add("QR0");
                    qr2NodeList.add("QR1");
                    qr2NodeList.add("QR2");
                    targetMObjects = getTargetMObjects(qr2NodeList, savedTaskDataList);
                    break;
                case "MQR":
                    List<String> mqrNodeList = new ArrayList<>();
                    mqrNodeList.add("QR0");
                    mqrNodeList.add("QR1");
                    mqrNodeList.add("QR2");
                    mqrNodeList.add("MQR");
                    targetMObjects = getTargetMObjects(mqrNodeList, savedTaskDataList);
                    break;
                default:
                    throw new TranscendBizException("数据异常");
            }

            if (CollectionUtil.isNotEmpty(targetMObjects)) {
                addExpandAo.setTargetMObjects(targetMObjects);
                baseApmSpaceAppRelationDataDrivenService.addExpand(authSpaceBid, authSpotCheckAppBid, source, addExpandAo);
            }

        }
    }


    private List<MObject> transcendTaskQuery(String belongProjectBid) {
        QueryWrapper queryTargetWrapper = new QueryWrapper();
        queryTargetWrapper.eq("belongProjectBid", belongProjectBid).and().eq("deleteFlag", 0);
        List<QueryWrapper> queryTarget = QueryWrapper.buildSqlQo(queryTargetWrapper);
        List<MObject> targetList = objectModelCrudI.list(targetModelCode, queryTarget);
        return targetList;
    }

    private List<MObject> transcendPointCheckQuery(String belongProjectBid) {
        QueryWrapper queryTargetWrapper = new QueryWrapper();
        queryTargetWrapper.eq("belongProjectBid", belongProjectBid).and().eq("deleteFlag", 0);
        List<QueryWrapper> queryTarget = QueryWrapper.buildSqlQo(queryTargetWrapper);
        List<MObject> targetList = objectModelCrudI.list(sourceModelCode, queryTarget);
        return targetList;
    }

    private List<MSpaceAppData> getTargetMObjects(List<String> judgmentNodeList, List<MObject> savedTaskDataList) {
        List<MSpaceAppData> targetMObjects = Lists.newArrayList();
        for (Map<String, Object> task : savedTaskDataList) {
            String taskCheckPoint = (String) task.get("checkPoint");
            if (judgmentNodeList.contains(taskCheckPoint)) {
                MSpaceAppData data = new MSpaceAppData();
                Object taskBid = task.get("bid");
                Object taskDataBid = task.get("dataBid");
                data.put("bid", taskBid);
                data.put("dataBid", taskDataBid);
                data.put("enableFlag", "0");
                data.put("deleteFlag", "0");
                data.put("tenantId", "101");
                targetMObjects.add(data);
            }
        }
        return targetMObjects;
    }


    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        if (apmSpaceApp == null || CollectionUtils.isEmpty(mSpaceAppData)) {
            return false;
        }
        String spaceAppBid = authTaskAppBid;
        return spaceAppBid.equals(apmSpaceApp.getBid());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
