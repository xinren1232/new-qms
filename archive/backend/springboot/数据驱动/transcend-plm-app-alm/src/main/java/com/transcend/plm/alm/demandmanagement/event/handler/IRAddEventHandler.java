package com.transcend.plm.alm.demandmanagement.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description: 草稿数据的提交(用一句话描述该文件做什么)
 * IR新增数据事件处理器
 * @author sgx
 * date 2024/6/24 9:12
 * @version V1.0
 */
@Slf4j
@Component
public class IRAddEventHandler extends AbstractAddEventHandler {

    @Resource
    private SystemFeatureService  systemFeatureService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        //设置主导领域
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        String bid = param.getMSpaceAppData().getBid();
        if (StringUtils.isBlank(bid)) {
            return super.preHandle(param);
        }
        QueryWrapper qo = new QueryWrapper();
        qo.in("rr_bid", Lists.newArrayList(bid));
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> list = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(),queryWrappers);
        if (CollectionUtils.isNotEmpty(list)){
            list.stream()
                    .filter(mObject -> ObjectUtil.isNotEmpty(mObject.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()))
                            && (Integer) mObject.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()) == 1)
                    .findFirst()
                    .ifPresent(mObject -> mSpaceAppData.put(DemandManagementEnum.DOMINANT_DOMAIN.getCode(),
                            mObject.get("domainBid") != null ? mObject.get("domainBid") : ""));
        }
        // 建立关联特性关系
        if (ObjectUtil.isNotEmpty(mSpaceAppData.get(SystemFeatureConstant.MOUNT_SF))){
            Object mountSFBidData = mSpaceAppData.get(SystemFeatureConstant.MOUNT_SF);
            List<Object> mountSFBidObject = mountSFBidData instanceof List<?>
                    ? (List<Object>) mountSFBidData
                    : Collections.emptyList();
            Set<String> bids = mountSFBidObject.stream().map(v->{
                if (v instanceof String){
                    return v.toString();
                } else if (v instanceof List){
                    return ((List<?>)v).get(((List<?>) v).size()-1).toString();
                }
                return null;
            }).collect(Collectors.toSet());
            systemFeatureService.addSFRelation(mSpaceAppData, param.getSpaceBid(), bid, bids);
        } else if (ObjectUtil.isNotEmpty(mSpaceAppData.get(SystemFeatureConstant.belong_SF))) {
            systemFeatureService.addL2SFRelation(mSpaceAppData, param.getSpaceBid(), bid, mSpaceAppData.get(SystemFeatureConstant.belong_SF).toString());
        }
        return super.preHandle(param);
    }

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        return super.postHandle(param, result);
    }

    /**
     * 如果新增的是RR、IR对象实例，则返回true
     *
     * @param param 入参
     * @return boolean
     */
    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        return TranscendModel.IR.getCode().equals(param.getApmSpaceApp().getModelCode());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
