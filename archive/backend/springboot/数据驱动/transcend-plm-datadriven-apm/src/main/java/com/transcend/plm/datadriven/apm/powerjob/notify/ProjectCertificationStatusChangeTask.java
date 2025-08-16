package com.transcend.plm.datadriven.apm.powerjob.notify;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class ProjectCertificationStatusChangeTask implements BasicProcessor {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        //获取数据 expected_complete_time 预计完成时间
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            log.info("单机处理器====process=====ProjectCertificationStatusChangeTask process");
            //1.获取数据  is_overdue 是否超期
            String jobParams = taskContext.getJobParams();
            Map<String, String> mapParams = JSONObject.parseObject(jobParams, new TypeReference<Map<String, String>>() {});
            String targetModelCode = mapParams.getOrDefault(RelationEnum.MODEL_CODE.getColumn(),"");
            String spaceBid = mapParams.getOrDefault(RelationEnum.SPACE_BID.getColumn(),"");
            String spaceAppBid = mapParams.getOrDefault(RelationEnum.SPACE_APP_BID.getColumn(),"");
            QueryWrapper queryTargetWrapper = new QueryWrapper();
            if(spaceBid.isEmpty() && spaceAppBid.isEmpty()){
                queryTargetWrapper.notIn("life_cycle_code", Arrays.asList("NOT_INVOLVED","CANCELLED","PAUSE","CERTIFIED","CERTIFIED_REUSE"));
            }else{
                queryTargetWrapper.notIn("life_cycle_code", Arrays.asList("NOT_INVOLVED","CANCELLED","PAUSE","CERTIFIED","CERTIFIED_REUSE")).and().eq(RelationEnum.SPACE_BID.getColumn(),spaceBid).and()
                        .eq(RelationEnum.SPACE_APP_BID.getColumn(), spaceAppBid);
            }
            List<QueryWrapper> queryTarget = QueryWrapper.buildSqlQo(queryTargetWrapper);
            //查询实例的数据；
            List<MObject> targetList = objectModelCrudI.list(targetModelCode, queryTarget);
            //2.筛选出有效数据
            List<String> bids =new ArrayList<>();
            targetList.forEach(p->{
                //比较日期是否相同  当天不超期
                LocalDateTime expectedCompleteTime = (LocalDateTime)p.getOrDefault("expectedCompleteTime",null);
                if(null != expectedCompleteTime){
                    expectedCompleteTime = LocalDateTime.of(LocalDate.parse(expectedCompleteTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MAX);
                    if(currentDate.isAfter(expectedCompleteTime)){
                        //当前日期在限定提起后面，超期了
                        bids.add(p.getBid());
                    }
                }
            });
            if(!bids.isEmpty()){
                MObject updateObject = new MObject();
                updateObject.put("isOverDate","是");
                Boolean b = objectModelCrudI.batchUpdatePartialContentByIds(targetModelCode, updateObject, bids);
                return new ProcessResult(b, "ExecuteTriggerNotify process success");
            }
        } catch (Exception e) {
                log.error("定时任务执行失败", e);
        }
       return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }
}
