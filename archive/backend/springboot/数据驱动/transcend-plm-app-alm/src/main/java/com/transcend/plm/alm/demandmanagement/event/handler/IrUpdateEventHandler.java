
package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transsion.framework.common.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Describe Ar修改事件
 * @Author haijun.ren
 * @Date 2024/9/27
 */
@Slf4j
@Component
public class IrUpdateEventHandler extends AbstractUpdateEventHandler {

    @Resource
    private SystemFeatureService systemFeatureService;

    @Override
    public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
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
            systemFeatureService.addSFRelation(mSpaceAppData, param.getSpaceBid(), param.getBid(), bids);
        } else if (mSpaceAppData.containsKey(SystemFeatureConstant.MOUNT_SF)){
            systemFeatureService.addSFRelation(mSpaceAppData, param.getSpaceBid(), param.getBid(), new HashSet<>());
        }
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        CfgObjectVo cfgObjectVo = param.getCfgObjectVo();
        if(cfgObjectVo != null){
            return TranscendModel.IR.getCode().equals(cfgObjectVo.getModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
