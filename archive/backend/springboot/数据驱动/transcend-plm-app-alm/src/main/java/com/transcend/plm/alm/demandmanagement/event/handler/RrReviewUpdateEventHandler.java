package com.transcend.plm.alm.demandmanagement.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckEnum;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.sdk.push.domain.message.element.Button;
import com.transsion.sdk.push.domain.message.element.Text;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Describe RR评审信息更新事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class RrReviewUpdateEventHandler extends AbstractUpdateEventHandler {

    /**
     * TARGET_MODELCODE
     */
    private static final String TARGET_MODELCODE = "A6G";

    /**
     * RELATION_MODELCODE
     */
    private static final String RELATION_MODELCODE = "A6H";

    /**
     * SOURCE_MODELCODE
     */
    private static final String SOURCE_MODELCODE = "A5E";

    /**
     * TARGET_MODELCODE
     */
    private static final String RR_TODO_PERSON = "toDoPerson";

    @Value("${apm.web.url:https://alm.transsion.com/#}")
    private String apmWebUrl;

    @Value("${apm.instance.web.path:/share/info/%s/%s/%s?viewMode=tableView}")
    private String apmInstanceWebPathTemplate;

    @Value("${apm.msg.picture.url:https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1702983291787/20231219-185031.png}")
    private String apmMsgPictureUrl;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private RuntimeService runtimeService;
    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        if(mSpaceAppData != null){
            //更新RR代办人
            if (mSpaceAppData.containsKey("toDoPerson") && ObjectUtils.isNotEmpty(mSpaceAppData.containsKey("toDoPerson"))){
                QueryWrapper qo = new QueryWrapper();
                qo.in(RelationEnum.TARGET_BID.getColumn(), mSpaceAppData.getBid());
                List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
                List<MObject> relationList = objectModelCrudI.listByQueryWrapper(RELATION_MODELCODE,queryWrappers);
                if(CollectionUtils.isNotEmpty(relationList)) {
                    String sourceBid = relationList.get(0).get(RelationEnum.SOURCE_BID.getCode()) + "";
                    MObject sourceData = objectModelCrudI.getByBid(SOURCE_MODELCODE, sourceBid);
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
                    MObject updateMobject = new MObject();
                    updateMobject.put("toDoPerson",toDoPersons);
                    List<String> sourceBids = new ArrayList<>();
                    sourceBids.add(sourceBid);
                    objectModelCrudI.batchUpdatePartialContentByIds(SOURCE_MODELCODE,updateMobject,sourceBids);
                    if (CollectionUtils.isNotEmpty(toDoPersons)) {
                        runtimeService.updateFlowRoleUsers(sourceBid,toDoPersons,sourceData.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()).toString(),sourceData.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()).toString(), RR_TODO_PERSON);
                    }
                    //如果新增了代办人，需要发消息
                    List<String> oldToDoPerson = TranscendTools.analysisPersions(sourceData.get(("toDoPerson")));
                    List<String> addToDoPerson = TranscendTools.analysisPersions(mSpaceAppData.get(("toDoPerson")));
                    List<String> add = addToDoPerson.stream().filter(s -> !oldToDoPerson.contains(s)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(add)) {
                        //发送通知
                        MObject mObject = targetList.stream().filter(v -> v.getBid().equals(mSpaceAppData.getBid())).findFirst().orElse(new MObject());
                        String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, sourceData.get(SpaceAppDataEnum.SPACE_BID.getCode()), SpaceAppDataEnum.SPACE_APP_BID.getCode(), sourceBid);
                        String title = String.format("【%s】的待办：%s", sourceData.getName(),mObject.get("toDoItems"));
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
            }
            //最后一个数据更新IR
            if(mSpaceAppData.containsKey("requirementlevel") || mSpaceAppData.containsKey("priorityOfRequirementVerification")){
               //判断当前数据是不是最后一个数据
                QueryWrapper qo = new QueryWrapper();
                qo.in(RelationEnum.TARGET_BID.getColumn(), mSpaceAppData.getBid());
                List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
                List<MObject> relationList = objectModelCrudI.listByQueryWrapper(RELATION_MODELCODE,queryWrappers);
                if(CollectionUtils.isNotEmpty(relationList)) {
                    QueryWrapper resQo = new QueryWrapper();
                    String sourceBid = relationList.get(0).get(RelationEnum.SOURCE_BID.getCode()) + "";
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
                    Order order = new Order();
                    order.setColumn("created_time");
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    queryCondition.setOrders(orders);
                    List<MObject> targetList = objectModelCrudI.listByQueryCondition(TARGET_MODELCODE, queryCondition);
                    if (CollectionUtils.isNotEmpty(targetList)) {
                        //判断当前修改的是不是最后一条数据
                        if (mSpaceAppData.getBid().equals(targetList.get(targetList.size() - 1).get(TranscendModelBaseFields.BID) + "")) {
                            //更新源数据
                            MObject updateMobject = new MObject();
                            if (mSpaceAppData.containsKey("requirementlevel")) {
                                updateMobject.put("requirementlevel", mSpaceAppData.get("requirementlevel"));
                            }
                            if (mSpaceAppData.containsKey("priorityOfRequirementVerification")) {
                                updateMobject.put("priority", mSpaceAppData.get("priorityOfRequirementVerification"));
                            }
                            List<String> sourceBids = new ArrayList<>();
                            sourceBids.add(sourceBid);
                            objectModelCrudI.batchUpdatePartialContentByIds(SOURCE_MODELCODE, updateMobject, sourceBids);
                        }
                    }
                }
            }
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        CfgObjectVo cfgObjectVo = param.getCfgObjectVo();
        if(cfgObjectVo != null){
            return TARGET_MODELCODE.equals(cfgObjectVo.getModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
