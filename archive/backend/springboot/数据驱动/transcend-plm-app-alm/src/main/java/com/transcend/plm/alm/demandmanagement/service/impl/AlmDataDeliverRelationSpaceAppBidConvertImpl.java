package com.transcend.plm.alm.demandmanagement.service.impl;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverSpecialConverter;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 关联数据无空间bid时转换处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/17 11:51
 */
@Service
@AllArgsConstructor
public class AlmDataDeliverRelationSpaceAppBidConvertImpl implements AlmDataDeliverSpecialConverter {
    /**
     * 需要进行处理的模型编码
     */
    private final TranscendModel[] TRANSCEND_MODELS = {TranscendModel.RELATION_IR_SR, TranscendModel.RELATION_SR_AR};
    private final ObjectModelStandardI<MObject> objectModelCrudI;
    private final CfgObjectRelationFeignClient cfgObjectRelationClient;

    /**
     * 关系编码对应源对象模型编码
     * 编码使用频率高，且一般不发生变动，故放在内存中缓存
     * key:关系编码
     * value:源对象模型编码
     */
    private final Map<String, String> relationSourceModelCodeMap = new HashMap<>();


    @Override
    public void convert(TranscendObjectWrapper data) {
        TranscendRelationWrapper relation = new TranscendRelationWrapper(data);
        String sourceBid = relation.getSourceBid();
        if (sourceBid == null) {
            return;
        }
        String sourceModelCode = getRelationSourceModelCode(relation.getModelCode());
        MObject object = objectModelCrudI.getByBid(sourceModelCode, sourceBid);
        Optional.ofNullable(object).map(TranscendObjectWrapper::new)
                .map(TranscendObjectWrapper::getSpaceAppBid)
                .ifPresent(data::setSpaceAppBid);
    }

    @Override
    public boolean isSupport(TranscendObjectWrapper entity) {
        if (entity == null) {
            return false;
        }
        return TranscendModel.matchCode(entity.getModelCode(), TRANSCEND_MODELS);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 获取关系对象源对象的模型编码
     *
     * @param relationModelCode 关系对象模型编码
     * @return 源对象模型编码
     */
    private String getRelationSourceModelCode(String relationModelCode) {
        return relationSourceModelCodeMap.computeIfAbsent(relationModelCode, key -> {
            CfgObjectRelationVo relation = cfgObjectRelationClient.getRelation(key)
                    .getCheckExceptionData();
            Assert.notNull(relation, "关系对象模型编码不存在");
            return relation.getSourceModelCode();
        });
    }
}
