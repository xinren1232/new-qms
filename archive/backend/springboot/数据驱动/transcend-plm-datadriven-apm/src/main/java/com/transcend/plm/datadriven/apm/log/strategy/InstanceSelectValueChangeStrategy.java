package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.netflix.discovery.converters.Auto;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Qiu Yuhao
 * @Date 2024/2/1 9:35
 * @Describe
 */
@Component(ViewComponentEnum.INSTANCE_SELECT_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class InstanceSelectValueChangeStrategy extends AbstractValueChangeStrategy {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI<MObject> objectModelStandardI;

    @Resource
    private CfgViewFeignClient cfgViewFeignClient;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;


    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        List<String> oldValueList = Lists.<String>newArrayList();
        List<String> newValueList = Lists.<String>newArrayList();
        if (isArray(oldValue)) {
            oldValueList = transferArrayValue(oldValue, String.class);
        } else {
            oldValueList.add(String.valueOf(oldValue));
        }
        if (isArray(newValue)) {
            newValueList = transferArrayValue(newValue, String.class);
        } else {
            newValueList.add(String.valueOf(newValue));
        }
        List<String> oldValueListCp = Lists.<String>newArrayList(oldValueList);
        List<String> newValueListCp = Lists.<String>newArrayList(newValueList);
        // 判断是否有变化 则直接用两个集合相互取差集 如果都为空则没有变化
        oldValueListCp.removeAll(newValueList);
        newValueListCp.removeAll(oldValueList);
        if (oldValueListCp.isEmpty() && newValueListCp.isEmpty()) {
            return null;
        }
        // 查询视图信息
        String instanceSelectAppBid = cfgViewMetaDto.getInstanceSelectAppBid();
        ApmSpaceApp app = apmSpaceAppService.getByBid(instanceSelectAppBid);
        String modelCode = app.getModelCode();
        List<MObject> oldObjectList = objectModelStandardI.listByBids(oldValueList, modelCode);
        List<MObject> newObjectList = objectModelStandardI.listByBids(newValueList, modelCode);
        String oldValueResult = oldObjectList.stream().map(MObject::getName).collect(Collectors.joining(","));
        String newValueResult = newObjectList.stream().map(MObject::getName).collect(Collectors.joining(","));
        return super.getChangeValue(spaceAppBid, oldValueResult, newValueResult, cfgViewMetaDto);
    }
}
