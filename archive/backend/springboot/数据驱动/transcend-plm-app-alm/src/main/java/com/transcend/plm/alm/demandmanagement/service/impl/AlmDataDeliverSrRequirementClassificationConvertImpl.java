package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.convert.Convert;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverSpecialConverter;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendBaseWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 需求分类数据转换处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/11 09:31
 */
@Service
@AllArgsConstructor
public class AlmDataDeliverSrRequirementClassificationConvertImpl implements AlmDataDeliverSpecialConverter {
    private final String FIELD = "srRequirementClassification";
    private final ObjectModelStandardI<MObject> objectModelCrudI;


    @Override
    public void convert(TranscendObjectWrapper data) {
        Object value = data.get(FIELD);
        String modelCode = TranscendModel.REQUIREMENT_CLASSIFICATION.getCode();
        String outputField = "name";

        if (value instanceof String) {
            String strValue = data.getStr(FIELD);
            if (StringUtils.isBlank(strValue)) {
                return;
            }
            MObject object = objectModelCrudI.getByBid(modelCode, strValue);
            Optional.ofNullable(object).map(TranscendObjectWrapper::new)
                    .ifPresent(wrapper -> data.put(FIELD, object.get(outputField)));
            return;
        }

        if (value instanceof Collection) {
            List<String> bidList = Convert.toList(String.class, value);
            QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getColumn(), bidList);
            Map<String, Object> dataMap = objectModelCrudI.list(modelCode,
                            QueryWrapper.buildSqlQo(wrapper)).stream().map(TranscendObjectWrapper::new)
                    .collect(Collectors.toMap(TranscendBaseWrapper::getBid, t -> t.get(outputField), (v1, v2) -> v1));
            data.put(FIELD, bidList.stream().map(bid -> dataMap.getOrDefault(bid, bid)).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean isSupport(TranscendObjectWrapper entity) {
        //包含该字段
        Object value = entity.get(FIELD);
        if (value == null) {
            return false;
        }

        if (!(value instanceof String || value instanceof Collection)) {
            return false;
        }

        return entity.getModelCode().equals(TranscendModel.SR.getCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
