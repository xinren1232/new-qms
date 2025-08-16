package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.vo.OperationLogVo;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.domain.object.base.pojo.dto.OperationLog;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.BaseDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Slf4j
@Service
public class OperationLogService {
    @Resource
    private BaseDataRepository baseDataRepository;

    /**
     * @param tenantCode
     * @param hasCreatedTime
     * @return {@link TableDefinition }
     */
    private TableDefinition getTableDefinition(String tenantCode,boolean hasCreatedTime){
        TableDefinition tableDefinition = TableDefinition.of()
                .setLogicTableName(tenantCode+"_instance_operation_log")
                .setTenantCode("transcend").setUseLogicTableName(DataBaseConstant.BYTE_TRUE);
        // 表属性定义
        List<TableAttributeDefinition> attrs = new ArrayList<>();
        TableAttributeDefinition attrBid = TableAttributeDefinition.of()
                .setColumnName(TranscendModelBaseFields.BID).setProperty(TranscendModelBaseFields.BID).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition attrModelCode = TableAttributeDefinition.of()
                .setColumnName("model_code").setProperty(TranscendModelBaseFields.MODEL_CODE).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition instanceDataBid = TableAttributeDefinition.of()
                .setColumnName("instance_data_bid").setProperty("instanceDataBid").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition instanceBid = TableAttributeDefinition.of()
                .setColumnName("instance_bid").setProperty("instanceBid").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition content = TableAttributeDefinition.of()
                .setColumnName("content").setProperty("content").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition createdBy = TableAttributeDefinition.of()
                .setColumnName("created_by").setProperty(TranscendModelBaseFields.CREATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition createdByName = TableAttributeDefinition.of()
                .setColumnName("created_by_name").setProperty("createdByName").setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        TableAttributeDefinition updatedBy = TableAttributeDefinition.of()
                .setColumnName("updated_by").setProperty(TranscendModelBaseFields.UPDATED_BY).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
        attrs.add(attrBid);
        attrs.add(attrModelCode);
        attrs.add(instanceDataBid);
        attrs.add(instanceBid);
        attrs.add(content);
        attrs.add(createdBy);
        attrs.add(createdByName);
        attrs.add(updatedBy);
        if(hasCreatedTime){
            TableAttributeDefinition createdTime = TableAttributeDefinition.of()
                    .setColumnName("created_time").setProperty(TranscendModelBaseFields.CREATED_TIME).setBaseFlag(DataBaseConstant.BASE_COLUMN_FLAG);
            attrs.add(createdTime);
        }
        // 给表定义设置表属性定义
        tableDefinition.setTableAttributeDefinitions(attrs);
        return tableDefinition;
    }

    /**
     * @param operationLog
     * @return {@link MObject }
     */
    private MObject getMObject(OperationLog operationLog){
        MObject mBaseData = new MObject();
        mBaseData.setBid(SnowflakeIdWorker.nextIdStr());
        mBaseData.setModelCode(operationLog.getModelCode());
        mBaseData.put("instanceDataBid",operationLog.getInstanceDataBid());
        mBaseData.put("instanceBid",operationLog.getInstanceBid());
        mBaseData.put("content",operationLog.getContent());
        mBaseData.setCreatedBy(operationLog.getCreatedBy());
        mBaseData.put("createdByName",operationLog.getCreatedByName());
        mBaseData.setUpdatedBy(operationLog.getCreatedBy());
        return mBaseData;
    }

    /**
     * @param operationLog
     * @return boolean
     */
    public boolean saveOperationLog(OperationLog operationLog){
        try{
            CompletableFuture.runAsync(
                    () -> {
                        TableDefinition tableDefinition = getTableDefinition(operationLog.getTenantCode(),false);
                        // 实例数据
                        MObject mBaseData = getMObject(operationLog);
                        baseDataRepository.add(tableDefinition, mBaseData);
                    });
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return true;
    }

    /**
     * @param tenantCode
     * @param modelCode
     * @param instance_data_bid
     * @return {@link List }<{@link OperationLogVo }>
     */
    public List<OperationLogVo> getOperationLogs(String tenantCode, String modelCode, String instance_data_bid){
        TableDefinition tableDefinition = getTableDefinition(tenantCode,true);
        QueryWrapper qo = new QueryWrapper();
        qo.eq(ObjectEnum.MODEL_CODE.getCode(), modelCode).and().eq("instanceDataBid",instance_data_bid);
        List<Order> orders = new ArrayList<>();
        Order order = Order.of().setProperty(BaseDataEnum.CREATED_TIME.getCode()).setDesc(true);
        orders.add(order);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        QueryCondition queryCondition = QueryCondition.of()
                .setQueries(queryWrappers).setOrders(orders);
        List<MBaseData> list = baseDataRepository.list(tableDefinition, queryCondition);
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        List<OperationLogVo> resList = new ArrayList<>();
        List<String> days = new ArrayList<>();
        Map<String,List<MBaseData>> resMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(MBaseData mBaseData:list){
            LocalDateTime craetedTime = (LocalDateTime) mBaseData.get(TranscendModelBaseFields.CREATED_TIME);
            String day = craetedTime.format(formatterDay);
            String time = craetedTime.format(formatterTime);
            List<MBaseData> mBaseDataList = resMap.get(day);
            if(mBaseDataList == null){
                mBaseDataList = new ArrayList<>();
            }
            mBaseData.put("createdTimeStr",time);
            mBaseDataList.add(mBaseData);
            resMap.put(day,mBaseDataList);
            if(!days.contains(day)){
                days.add(day);
            }
        }
        for(String day:days){
            OperationLogVo operationLogVo = new OperationLogVo();
            operationLogVo.setDate(day);
            operationLogVo.setData(resMap.get(day));
            resList.add(operationLogVo);
        }
        return resList;
    }
}
