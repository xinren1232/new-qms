package com.transcend.plm.alm.demandmanagement.event.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.entity.bo.RelDemandBo;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.constants.TodoCenterConstant;
import com.transcend.plm.datadriven.apm.event.entity.BatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractBatchDeleteEventHandler;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.service.IUacUserService;
import com.transsion.sdk.push.domain.message.element.Button;
import com.transsion.sdk.push.domain.message.element.Text;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.*;

/**
 * @Describe RR评审信息关系新增处理事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class RrBatchDeleteEventHandler extends AbstractBatchDeleteEventHandler {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private DemandManagementService demandManagementService;

    @Resource
    private DemandManagementProperties demandManagementProperties;

    private static final String RR_OBJ_BID = "1253640663282315264";

    @Value("${apm.web.url:https://alm.transsion.com/#}")
    private String apmWebUrl;

    @Resource
    private IUacUserService iUacUserService;

    /**
     * 依次为空间bid，应用bid，实例bid
     */
    @Value("${apm.instance.web.path:/share/info/%s/%s/%s?viewMode=tableView}")
    private String apmInstanceWebPathTemplate;
    @Value("${apm.msg.picture.url:https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1702983291787/20231219-185031.png}")
    private String apmMsgPictureUrl;
    @Value("${apm.flow.todoCenter.appName:Transcend}")
    private String todoCenterAppName;


    @Override
    public BatchDeleteEventHandlerParam preHandle(BatchDeleteEventHandlerParam param) {
        //组装后置事件参数
        List<String> bids = param.getBids();
        Map<String,List<MObject>> irMap = new HashMap<>();
        for(String bid:bids){
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.like("rr_original_requirement_number", bid).and().eq("delete_flag",0);
            List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
            List<MObject> irList = objectModelCrudI.list(TranscendModel.IR.getCode(),wrappers);
            irMap.put(bid,irList);
        }
        param.setIrMap(irMap);
        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        //查询删除的RR关联的IR实例列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in(RelationEnum.SOURCE_BID.getCode(), param.getBids());
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(queryWrapper);
        List<MObject> dataList= (List<MObject>) objectModelCrudI.list(TranscendModel.RELATION_RR_IR.getCode(), wrappers);
        List<String> targetBids = new ArrayList<>();
        for (MObject mObject : dataList) {
            targetBids.add((String) mObject.get(RelationEnum.TARGET_BID.getCode()));
        }
        if (!targetBids.isEmpty()) {
            List<MObject> mObjects = objectModelCrudI.listByBids(targetBids, TranscendModel.IR.getCode());
            List<BatchUpdateBO<MSpaceAppData>> batchUpdateBoList = Lists.newArrayList();
            for (MObject mObject : mObjects) {
                MSpaceAppData updateMobject = new MSpaceAppData();
                Object rrOriginalRequirementNumber = mObject.get("rrOriginalRequirementNumber");
                Object demandOrigin = mObject.get("demandOrigin");
                if (ObjectUtils.isNotEmpty(rrOriginalRequirementNumber)) {
                    BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
                    QueryWrapper updateWrapper = new QueryWrapper(Boolean.FALSE);
                    updateWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                    updateWrapper.setCondition("=");
                    updateWrapper.setValue(mObject.getBid());
                    batchUpdateBO.setWrappers(Collections.singletonList(updateWrapper));
                    //需求来源
                    List<String> demandOriginList = new ArrayList<>();
                    //rrBid
                    List<String> rrlist = new ArrayList<>();
                    try {
                        rrlist = JSON.parseArray(rrOriginalRequirementNumber.toString(), String.class);
                    } catch (JSONException e) {
                        rrlist.add(rrOriginalRequirementNumber.toString());
                    }
                    try {
                        demandOriginList = JSON.parseArray(demandOrigin.toString(), String.class);
                    } catch (JSONException e) {
                        demandOriginList.add(demandOrigin.toString());
                    }
                    if (rrlist.size() ==1 && param.getBids().contains(rrlist.get(0))) {
                        updateMobject.put("rrOriginalRequirementNumber", new ArrayList<>());
                        demandOriginList.remove("rr");
                        updateMobject.put("demandOrigin", demandOriginList);
                        batchUpdateBO.setBaseData(updateMobject);
                        batchUpdateBoList.add(batchUpdateBO);
                    }else if (rrlist.size() > 1 && rrlist.removeAll(param.getBids())) {
                        updateMobject.put("rrOriginalRequirementNumber", rrlist);
                        if (rrlist.isEmpty()) {
                            demandOriginList.remove("rr");
                        }
                        updateMobject.put("demandOrigin", demandOriginList);
                        batchUpdateBO.setBaseData(updateMobject);
                        batchUpdateBoList.add(batchUpdateBO);
                    }
                }
            }
            if (!batchUpdateBoList.isEmpty()) {
                objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.IR.getCode(), batchUpdateBoList, false);
            }
        }
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(BatchDeleteEventHandlerParam param, Boolean result) {
        CompletableFuture.runAsync(() -> pushMessage(param,result), SimpleThreadPool.getInstance());
        return super.postHandle(param, result);
    }

    public void pushMessage(BatchDeleteEventHandlerParam param, Boolean result){
        //查模块数据
        //查IR数据
        List<String> bids = param.getBids();
        Map<String,List<MObject>> irMap = param.getIrMap();
        for(String bid:bids){
            MObject rrMobject = objectModelCrudI.getByBidNotDelete(TranscendModel.RR.getCode(),bid);
            OpenResponse<UserDTO> openResponse = iUacUserService.queryDetail(rrMobject.getCreatedBy());
            String userName = rrMobject.getCreatedBy();
            if(openResponse != null && openResponse.getData() != null){
                userName = openResponse.getData().getRealName();
            }
            List<MObject> irList = irMap.get(bid);
            if(CollectionUtils.isNotEmpty(irList)){
                for(MObject mObject:irList){
                    List<String> rrBids = JSON.parseObject(
                            JSON.toJSONString(mObject.getOrDefault(RR_ORIGINAL_REQUIREMENT_NUMBER, new JSONArray())),
                            new TypeReference<List<String>>() {}
                    );
                    if(CollectionUtils.isNotEmpty(rrBids) && rrBids.size() == 1 && rrBids.contains(bid)){
                        String createdBy = mObject.getCreatedBy();
                        if(StringUtils.isNotBlank(createdBy)){
                            String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mObject.get("spaceBid"), mObject.get("spaceAppBid"), mObject.getBid());
                            //去掉零宽空格
                            url = url.replaceAll("\\u200B","").trim();
                            List<String> empNos = Lists.newArrayList(createdBy);
                            //去掉零宽空格
                            String title = mObject.getName();
                            String content = "RR需求【"+rrMobject.getName()+"】已被创建人【"+userName+"】删除，请关注并查看IR需求是否需要更新";
                            PushCenterFeishuBuilder feishuBuilder = PushCenterFeishuBuilder.builder().title(title, "red")
                                    .image(title, apmMsgPictureUrl)
                                    .dividingLine()
                                    .content(content)
                                    .dividingLine()
                                    .action(Lists.newArrayList(Button.builder().text(Text.builder().content("前往处理\uD83D\uDE80").build()).url(url).style("primary").build()))
                                    .url(url)
                                    .receivers(empNos);
                            NotifyEventBus.EVENT_BUS.post(feishuBuilder);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isMatch(BatchDeleteEventHandlerParam param) {
        String objBid = param.getObjBid();
        return RR_OBJ_BID.equals(objBid);
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
