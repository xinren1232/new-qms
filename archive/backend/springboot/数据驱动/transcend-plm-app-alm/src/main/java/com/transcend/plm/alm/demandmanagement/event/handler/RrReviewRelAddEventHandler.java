package com.transcend.plm.alm.demandmanagement.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckEnum;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.sdk.push.domain.message.element.Button;
import com.transsion.sdk.push.domain.message.element.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Describe RR评审信息关系新增处理事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class RrReviewRelAddEventHandler extends AbstractRelationAddEventHandler {

    /**
     * RR评审信息关系MODELCODE
     */
    private static final String RELATION_MODELCODE = "A6H";

    /**
     * TARGET_MODELCODE
     */
    private static final String TARGET_MODELCODE = "A6G";


    @Value("${apm.web.url:https://alm.transsion.com/#}")
    private String apmWebUrl;

    @Value("${apm.instance.web.path:/share/info/%s/%s/%s?viewMode=tableView}")
    private String apmInstanceWebPathTemplate;

    @Value("${apm.msg.picture.url:https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1702983291787/20231219-185031.png}")
    private String apmMsgPictureUrl;

    /**
     * TARGET_MODELCODE
     */
    private static final String RR_TODO_PERSON = "toDoPerson";

    /**
     * 源MODELCODE
     */
    private static final String SOURCE_MODELCODE = "A5E";
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private RuntimeService runtimeService;

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        return super.preHandle(param);
    }

    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
        if(CollectionUtils.isNotEmpty(targetMObjects) && targetMObjects.stream().anyMatch(v->FlowCheckEnum.PPEOFRAT.getCode().equals(v.get("reviewType")))){
            //更新RR代办人
            QueryWrapper resQo = new QueryWrapper();
            resQo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
            List<QueryWrapper> relQueryWrappers = QueryWrapper.buildSqlQo(resQo);
            List<MObject> allRelationList = objectModelCrudI.listByQueryWrapper(RELATION_MODELCODE, relQueryWrappers);
            List<String> targetBids = new ArrayList<>();
            for (MObject relation : allRelationList) {
                targetBids.add(relation.get(RelationEnum.TARGET_BID.getCode()) + "");
            }
            QueryWrapper targetQo = new QueryWrapper();
            targetQo.in(RelationEnum.BID.getColumn(), targetBids).and().eq("delete_flag", "0");
            List<QueryWrapper> targetQoQueryWrappers = QueryWrapper.buildSqlQo(targetQo);
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setQueries(targetQoQueryWrappers);
            List<MObject> targetList = objectModelCrudI.listByQueryCondition(TARGET_MODELCODE, queryCondition);
            Set<String> toDoPersonSet = new HashSet<>();
            for (MObject target : targetList) {
                if (FlowCheckEnum.PPEOFRAT.getCode().equals(target.get("reviewType"))) {
                    List<String> objList = TranscendTools.analysisPersions(target.get(("toDoPerson")));
                    if (CollectionUtils.isNotEmpty(objList)) {
                        toDoPersonSet.addAll(objList);
                    }
                }
            }
            List<String> toDoPersons = new ArrayList<>(toDoPersonSet);
            //将最新一条的IR评审信息的 需求优先级和需求等级更新到 IR实例中
            MSpaceAppData targetMObject = targetMObjects.get(targetMObjects.size()-1);
            MObject updateMobject = new MObject();
            updateMobject.put("requirementlevel",targetMObject.get("requirementlevel"));
            updateMobject.put("priority",targetMObject.get("priorityOfRequirementVerification"));
            updateMobject.put("toDoPerson",toDoPersons);
            List<String> sourceBids = new ArrayList<>();
            sourceBids.add(sourceBid);
            objectModelCrudI.batchUpdatePartialContentByIds(SOURCE_MODELCODE,updateMobject,sourceBids);
            if (CollectionUtils.isNotEmpty(toDoPersons)) {
                runtimeService.updateFlowRoleUsers(sourceBid,toDoPersons,addExpandAo.getSpaceBid(),addExpandAo.getSourceSpaceAppBid(), RR_TODO_PERSON);
            }
            if (CollectionUtils.isNotEmpty(toDoPersons)) {
                MObject sourceData = objectModelCrudI.getByBid(SOURCE_MODELCODE, sourceBid);
                MSpaceAppData mSpaceAppData = targetMObjects.stream().filter(v -> FlowCheckEnum.PPEOFRAT.getCode().equals(v.get("reviewType"))).findFirst().orElse(null);
                //发送通知
                String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, addExpandAo.getSpaceBid(), addExpandAo.getSourceSpaceAppBid(), sourceBid);
                String title = String.format("【%s】的待办：%s", sourceData.getName(),mSpaceAppData.get("toDoItems"));
                PushCenterFeishuBuilder feishuBuilder = PushCenterFeishuBuilder.builder().title(title, "red")
                        .image(title, apmMsgPictureUrl)
                        .dividingLine()
                        .content("您有一个新的任务需要处理，请前往【Transcend】工作台处理\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25")
                        .dividingLine()
                        .action(Lists.newArrayList(Button.builder().text(Text.builder().content("前往处理\uD83D\uDE80").build()).url(url).style("primary").build()))
                        .url(url)
                        .receivers(toDoPersons);
                NotifyEventBus.EVENT_BUS.post(feishuBuilder);
            }
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if (addExpandAo != null) {
            return RELATION_MODELCODE.equals(addExpandAo.getRelationModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
