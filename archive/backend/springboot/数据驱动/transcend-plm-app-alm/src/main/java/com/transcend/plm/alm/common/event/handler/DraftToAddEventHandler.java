package com.transcend.plm.alm.common.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * description: 草稿数据的提交(用一句话描述该文件做什么)
 *
 * 草稿数据提交变成实例数据的事件处理器
 * @author sgx
 * date 2024/6/24 9:12
 * @version V1.0
 */
@Component
public class DraftToAddEventHandler extends AbstractAddEventHandler {

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private DemandManagementProperties demandManagementProperties;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        // 提交状态是草稿
        String lifeCycleCode = param.getMSpaceAppData().getLifeCycleCode();
        if (CommonEnum.DRAFT.getCode().equals(lifeCycleCode)) {
            param.getMSpaceAppData().setLifeCycleCode(null);
        }
        // 提交时已有BID
        String bid = param.getMSpaceAppData().getBid();
        if (bid != null) {
            param.setDraftBid(bid);
            param.getMSpaceAppData().setBid(null);
            param.getMSpaceAppData().put(TranscendModelBaseFields.DATA_BID, null);
        }
        return super.preHandle(param);
    }

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        String spaceBid = param.getSpaceBid();
        String spaceAppBid = param.getApmSpaceApp().getBid();
        String newBid = result.getBid();
        if (StringUtils.isNotBlank(param.getDraftBid())) {
            iBaseApmSpaceAppDataDrivenService.logicalDelete(spaceAppBid, param.getDraftBid());
        }
        // RR 和IR 时 更新领域组件
        if (TranscendModel.RR.getCode().equals(param.getApmSpaceApp().getModelCode()) || TranscendModel.IR.getCode().equals(param.getApmSpaceApp().getModelCode())) {
            Set<String> domainBids = Sets.newHashSet();
            Set<String> domainUsers = Sets.newHashSet();
            Set<String> muduleBids = Sets.newHashSet();
            Set<String> productUsers = Sets.newHashSet();
            AtomicReference<String> leaderDomain = new AtomicReference<>();
            //领域组件
            String domainComponentModelCode = TranscendModel.DOMAIN_COMPONENT.getCode();
            //领域组件的数据
            List<MObject> domainComponentList = queryRelData(spaceBid, param.getDraftBid());
            List<BatchUpdateBO<MSpaceAppData>> batchUpdateBoList = Lists.newArrayList();
            if (CollectionUtils.isEmpty(domainComponentList)) {
                return super.postHandle(param, result);
            }
            domainComponentList.forEach(m -> {
                MSpaceAppData mSpaceAppData = MSpaceAppData.buildFrom(m,spaceBid,spaceAppBid);
                if (TranscendModel.DOMAIN.getCode().equals((String) m.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                    // 领域
                    domainBids.add( (String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    domainUsers.add((String) m.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
                    Integer isLeadDomain = (Integer) m.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode());
                    if (Integer.valueOf(1).equals(isLeadDomain)) {
                        leaderDomain.set((String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    }

                }else if ( TranscendModel.MODULE.getCode().equals((String) m.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                    // 模块
                    muduleBids.add((String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    productUsers.add((String) m.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
                }
                mSpaceAppData.put(DemandManagementEnum.RR_BID.getCode(), newBid);
                QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                queryWrapper.setCondition("=");
                queryWrapper.setValue(m.getBid());
                BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
                batchUpdateBO.setBaseData(mSpaceAppData);
                batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                batchUpdateBoList.add(batchUpdateBO);
            });
            objectModelCrudI.batchUpdateByQueryWrapper(domainComponentModelCode, batchUpdateBoList, false);
            MSpaceAppData appData = new MSpaceAppData();
            appData.setBid(newBid);
            //领域IDS
            appData.put(DemandManagementEnum.PRODUCT_AREA.getCode(),domainBids);
            // 主导领域
            appData.put(DemandManagementEnum.ONWENR.getCode(),leaderDomain.get());
            // 领域负责人
            appData.put(DemandManagementEnum.PRODUCT_MANAGER.getCode(),domainUsers);
            // 模块IDS
            appData.put(DemandManagementEnum.BELONG_MODULE.getCode(),muduleBids);
            // 产品负责人
            appData.put(DemandManagementEnum.PERSON_RESPONSIBLE.getCode(),productUsers);
            try {
                iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid,newBid,appData);
            } catch (Exception e) {
                // 不处理异常
            }
        }
        return super.postHandle(param, result);
    }

    private List<MObject> queryRelData(String spaceBid, String rrBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), spaceBid).and().eq(DemandManagementEnum.RR_BID.getColumn(), rrBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
    }

    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        String lifeCycleCode = param.getMSpaceAppData().getLifeCycleCode();
        String bid = param.getMSpaceAppData().getBid();
        return CommonEnum.DRAFT.getCode().equals(lifeCycleCode) && bid != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

