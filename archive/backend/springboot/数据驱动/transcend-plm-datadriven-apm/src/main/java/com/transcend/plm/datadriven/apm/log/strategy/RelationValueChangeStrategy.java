package com.transcend.plm.datadriven.apm.log.strategy;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 关系组件值变更策略(暂时先记name)
 * @author yinbin
 * @version:
 * @date 2023/10/19 17:05
 */
@Component(ViewComponentEnum.RELATION_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class RelationValueChangeStrategy extends AbstractValueChangeStrategy {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        if(Boolean.TRUE.equals(cfgViewMetaDto.getMultiple())){
            return getChangeValueByList(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
        }
        String sourceModelCode = cfgViewMetaDto.getSourceModelCode();
        if (Objects.nonNull(oldValue) && StringUtil.isNotBlank(oldValue.toString())) {
            MObject oldObject = objectModelCrudI.getByBid(sourceModelCode, (String) oldValue);
            oldValue = oldObject.getName();
        } else {
            oldValue = "";
        }
        if (Objects.nonNull(newValue) && StringUtil.isNotBlank(newValue.toString())) {
            MObject newObject = objectModelCrudI.getByBid(sourceModelCode, (String) newValue);
            newValue = newObject.getName();
        } else {
            newValue = "";
        }
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }

    public String getChangeValueByList(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        String sourceModelCode = cfgViewMetaDto.getSourceModelCode();
        List<String> oldValueList = (List<String>) oldValue;
        List<String> newValueList = (List<String>) newValue;
        if (Objects.nonNull(oldValue) && CollectionUtils.isNotEmpty(oldValueList)) {
            List<MObject> oldObjectList = objectModelCrudI.listByBids(oldValueList, sourceModelCode);
            oldValue = oldObjectList.stream().map(MObject::getName).collect(Collectors.joining(","));
        } else {
            oldValue = "";
        }
        if (Objects.nonNull(newValue) && CollectionUtils.isNotEmpty(newValueList)) {
            List<MObject> newObjectList = objectModelCrudI.listByBids(newValueList, sourceModelCode);
            newValue = newObjectList.stream().map(MObject::getName).collect(Collectors.joining(","));
        } else {
            newValue = "";
        }
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
