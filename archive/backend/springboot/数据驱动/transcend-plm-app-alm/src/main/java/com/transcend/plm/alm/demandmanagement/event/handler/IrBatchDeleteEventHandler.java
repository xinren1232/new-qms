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
import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.event.entity.BatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.BatchLogicDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractBatchDeleteEventHandler;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractBatchLogicDeleteEventHandler;
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

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.REL_DEMAND;
import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.RR_BID;

/**
 * @Describe RR评审信息关系新增处理事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class IrBatchDeleteEventHandler extends AbstractBatchLogicDeleteEventHandler {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private DemandManagementService demandManagementService;

    @Resource
    private DemandManagementProperties demandManagementProperties;

    private static final String IR_OBJ_BID = "1253649025684303872";

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
    public BatchLogicDeleteEventHandlerParam preHandle(BatchLogicDeleteEventHandlerParam param) {
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(BatchLogicDeleteEventHandlerParam param, Boolean result) {
        CompletableFuture.runAsync(() -> pushMessage(param,result), SimpleThreadPool.getInstance());
        return super.postHandle(param, result);
    }

    public void pushMessage(BatchLogicDeleteEventHandlerParam param, Boolean result){
        List<String> bids = param.getQo().getInsBids();
        Map<String, List<String>> rrBidIrNamesMap = new HashMap<>(16);
        for(String bid : bids){
            //查询删除的IR名称
            MObject irMobject = objectModelCrudI.getByBidNotDelete(TranscendModel.IR.getCode(),bid);
            //领域组件
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.like("rel_demand", bid).and().eq("delete_flag",0);
            List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
            List<MObject> domainList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(),wrappers);
            List<BatchUpdateBO<MObject>> updateList = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(domainList)){
                for(MObject mObject : domainList){
                    List<RelDemandBo> relDemandBos = JSON.parseObject(
                            JSON.toJSONString(mObject.getOrDefault(REL_DEMAND, new JSONArray())),
                            new TypeReference<List<RelDemandBo>>() {}
                    );
                    if(CollectionUtils.isNotEmpty(relDemandBos)){
                        if(relDemandBos.size() == 1 && relDemandBos.get(0).getRelDemandBid().equals(bid)){
                            if (rrBidIrNamesMap.containsKey(mObject.get("rrBid")+"")) {
                                rrBidIrNamesMap.get(mObject.get("rrBid")+"").add(irMobject.getName());
                            }else {
                                rrBidIrNamesMap.put(mObject.get("rrBid")+"", Lists.newArrayList(irMobject.getName()));
                            }
                        }
                        //处理rrDemand字段
                        boolean isUpdate = false;
                        for(int i = relDemandBos.size()- 1; i >= 0 ; i--){
                            if(relDemandBos.get(i).getRelDemandBid().equals(bid)){
                                relDemandBos.remove(i);
                                isUpdate = true;
                            }
                        }
                        if(isUpdate){
                            QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                            queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                            queryWrapper.setCondition("=");
                            queryWrapper.setValue(mObject.getBid());
                            BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
                            MObject updateObject = new MObject();
                            updateObject.put(REL_DEMAND, relDemandBos);
                            batchUpdateBO.setBaseData(updateObject);
                            batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                            updateList.add(batchUpdateBO);
                        }
                    }
                }
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(updateList)) {
                objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.DOMAIN_COMPONENT.getCode(), updateList, false);
            }
        }
        if(CollectionUtils.isNotEmpty(rrBidIrNamesMap)){
            //查询rr数据
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.in("bid", rrBidIrNamesMap.keySet()).and().eq("delete_flag",0);
            List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
            List<MObject> rrList = objectModelCrudI.list(TranscendModel.RR.getCode(),wrappers);
            if(CollectionUtils.isNotEmpty(rrList)) {
                for (MObject mObject : rrList) {
                    String createdBy = mObject.getCreatedBy();
                    if(StringUtils.isNotBlank(createdBy)){
                        String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mObject.get("spaceBid"), mObject.get("spaceAppBid"), mObject.getBid());
                        //去掉零宽空格
                        url = url.replaceAll("\\u200B","").trim();
                        List<String> empNos = Lists.newArrayList(createdBy);
                        //去掉零宽空格
                        String title = mObject.getName();
                        List<String> irNames = rrBidIrNamesMap.get(mObject.getBid());
                        String content = String.format("RR需求所关联的IR需求【%s】已被删除，请及时关注需求变化", String.join(",", irNames));
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

    @Override
    public boolean isMatch(BatchLogicDeleteEventHandlerParam param) {
        String objBid = param.getObjBid();
        return IR_OBJ_BID.equals(objBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
