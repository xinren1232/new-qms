package com.transcend.plm.alm.cad.event.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.context.EventData;
import com.transcend.plm.datadriven.apm.event.context.TranscendEventContext;
import com.transcend.plm.datadriven.apm.event.entity.CheckInEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractCheckInEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.constant.RelationConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transsion.framework.dto.BaseResponse;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author bin.yin
 * @description: cad 文档检入事件处理
 * @version:
 * @date 2024/06/13 17:39
 */
@Slf4j
@Component
public class CadDocCheckInEventHandler extends AbstractCheckInEventHandler {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;
    @Resource
    private ObjectModelDomainService objectModelDomainService;

    private static final String CAD_DOC_OBJ_BID = "1238071489643515904";

    private static final String CAD_REFRESH_CHANNEL = "/cad/refresh";

    @Override
    public CheckInEventHandlerParam preHandle(CheckInEventHandlerParam param) {
        String modelCode = param.getCfgObjectVo().getModelCode();
        List<ObjectRelationVO> objectRelationVoList = getObjectRelationVoList(modelCode);
        Optional<ObjectRelationVO> cadRefCadOptional = objectRelationVoList.stream().filter(e -> e.getSourceModelCode().equals(modelCode) && e.getTargetModelCode().equals(modelCode)).findAny();
        List<MObject> result = Lists.newArrayList();
        if (cadRefCadOptional.isPresent()) {
            ObjectRelationVO objectRelationVo = cadRefCadOptional.get();
            // 查询cad-cad关系数据，找出新增和移除的数据
            List<MObject> relDataList = listRelationDataBySourceBid((String) param.getMObject().get(VersionObjectEnum.DATA_BID.getCode()), objectRelationVo.getModelCode());
            // key=targetDataBid value=data
            Map<String, MObject> targetDataBidWithDataMap = Maps.newHashMap();
            // 检入前目标数据dataBid
            Set<String> beforeCheckInTargetDataBidList = Sets.newHashSet();
            // 检入后目标数据dataBid
            Set<String> afterCheckInTargetDataBidList = Sets.newHashSet();

            if (CollectionUtils.isNotEmpty(relDataList)) {
                relDataList.forEach(e -> {
                    String targetDataBid = (String) e.get(RelationObjectEnum.TARGET_DATA_BID.getCode());
                    targetDataBidWithDataMap.put(targetDataBid, e);
                    if (1 == ((Integer) e.get(RelationObjectEnum.DRAFT.getCode()))) {
                        afterCheckInTargetDataBidList.add(targetDataBid);
                    } else if (0 == ((Integer) e.get(RelationObjectEnum.DRAFT.getCode()))){
                        beforeCheckInTargetDataBidList.add(targetDataBid);
                    }
                });
                // 移除的关系
                Set<String> removeSet = new HashSet<>(beforeCheckInTargetDataBidList);
                removeSet.removeAll(afterCheckInTargetDataBidList);
                // 新增的关系
                Set<String> addSet = new HashSet<>(afterCheckInTargetDataBidList);
                addSet.removeAll(beforeCheckInTargetDataBidList);
                List<MObject> changeDataList = Lists.newArrayList();
                Map<String, String> typeMap = Maps.newHashMap();
                removeSet.forEach(targetDataBid -> {
                    MObject mObject = targetDataBidWithDataMap.get(targetDataBid);
                    typeMap.put(targetDataBid, "removeRel");
                    changeDataList.add(mObject);
                });
                addSet.forEach(targetDataBid -> {
                    MObject mObject = targetDataBidWithDataMap.get(targetDataBid);
                    typeMap.put(targetDataBid, "addRel");
                    changeDataList.add(mObject);
                });
                result = getObjectByDataBids(changeDataList, modelCode);
                result.forEach(e -> e.put("changeType", typeMap.get((String) e.get(VersionObjectEnum.DATA_BID.getCode()))));
            }
        }
        TranscendEventContext.set(new EventData<>(result));
        return super.preHandle(param);
    }

    @Override
    public MSpaceAppData postHandle(CheckInEventHandlerParam param, MSpaceAppData result) {
        log.info("cad检入后置通知redis, result: {}", JSON.toJSONString(result));
        EventData<List<MObject>> eventData = TranscendEventContext.get();
        MObject sendData = new MObject();
        sendData.putAll(result);
        if (eventData != null && CollectionUtils.isNotEmpty(eventData.getData())) {
            sendData.put("changeList", eventData.getData());
        }
        redisTemplate.convertAndSend(CAD_REFRESH_CHANNEL, sendData);
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(CheckInEventHandlerParam param) {
        CfgObjectVo objVo = param.getCfgObjectVo();
        String currentObjBid = objVo.getBid();
        return CAD_DOC_OBJ_BID.equals(currentObjBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private List<ObjectRelationVO> getObjectRelationVoList(String modelCode) {
        BaseResponse<List<ObjectRelationVO>> response = cfgObjectFeignClient.getObjectRelationVOsBySourceModelCode(modelCode);
        if (response == null || !Boolean.TRUE.equals(response.isSuccess())) {
            throw new PlmBizException("请求关系配置出错");
        }
        return response.getData();
    }

    private List<MObject> listRelationDataBySourceBid(String sourceDataBid, String relationModelCode){
        //先查询关系(历史)实例表
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_DATA_BID.getColumn(), sourceDataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mRelationObjectList;
        try {
            QueryCondition queryCondition = new QueryCondition();
            // 默认更新时间倒序
            queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            queryCondition.setQueries(queryWrappers);
            mRelationObjectList = objectModelDomainService.list(relationModelCode, queryCondition);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        return mRelationObjectList;
    }

    private List<MObject> getObjectByDataBids(List<MObject> mObjectList, String modelCode) {
        if (CollectionUtils.isEmpty(mObjectList)) {
            return Lists.newArrayList();
        }
        List<Object> targetDataBids = new ArrayList<>();
        for (MObject mObject : mObjectList) {
            targetDataBids.add(mObject.get("targetDataBid"));
        }
        //浮动关系直接根据dataBid查询
        QueryCondition targetDataQueryParam = new QueryCondition();
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.DATA_BID.getColumn(), targetDataBids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        targetDataQueryParam.setQueries(queryWrappers);
        List<MObject> resList = objectModelDomainService.list(modelCode, targetDataQueryParam);
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList) {
            relModelMap.put((String) mObject.get("targetDataBid"), mObject);
        }
        for (MObject mObject : resList) {
            mObject.put("dataSource", "instance");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get((String) mObject.get(TranscendModelBaseFields.DATA_BID)));
        }
        return resList;
    }
}
