package com.transcend.plm.datadriven.apm.strategy;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationObjectEnum;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.MEMBERSOFT_MODEL_CODE;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/15 14:03
 * @Describe
 */
@Slf4j
@Component
@DependsOn("modelCodeProperties")
public class AddOuterStrategy {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    private final Map<String, Function<AddExpandAo, Object>> strategyMap;

    {
        strategyMap = new HashMap<>();
        strategyMap.put(MEMBERSOFT_MODEL_CODE, this::roleAddStrategy);
    }

    /**
     * 获取对应的策略方法
     * @param addExpandAo 添加对象
     * @return T
     * @param <T> <T>
     */
    public <T> T execute(AddExpandAo addExpandAo) {
        // 通过ModelCode获取对应的策略方法
        String modelCode = addExpandAo.getTargetModelCode();
        Function<AddExpandAo, Object> targetFunction =
                Optional.ofNullable(modelCode).map(strategyMap::get).orElseThrow(() -> new PlmBizException("没有对应的策略方法"));
        return (T) targetFunction.apply(addExpandAo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean roleAddStrategy(AddExpandAo addExpandVo) {
        // 1. 先判断添加的角色是否存在
        QueryWrapper instanceWrapper = new QueryWrapper();
        // 给Wrapper添加前置条件 默认给1=2（or条件的场景）
        instanceWrapper.eq("1", 2);
        List<MSpaceAppData> targetMObjects = addExpandVo.getTargetMObjects();
        targetMObjects.forEach(data -> {
            initData(addExpandVo, data);
            String perno = (String) data.get("perno");
            instanceWrapper.or().eq("perno", perno);
        });
        // 查实例表
        List<MObject> existInstances = objectModelCrudI.listByQueryWrapper(addExpandVo.getTargetModelCode(), QueryWrapper.buildSqlQo(instanceWrapper));
        if (CollUtil.isNotEmpty(existInstances)) {
            // 2. 如果实例存在，则不重复添加
            existInstances.forEach(
                    instance -> targetMObjects.removeIf(data -> {
                        String perno = (String) data.get("perno");
                        return perno.equals(instance.get("perno"));
                    })
            );
        }
        // 3. 批量添加实例数据
        objectModelCrudI.addBatch(addExpandVo.getTargetModelCode(), targetMObjects);
        // 4. 查关系表
        QueryWrapper relationWrapper = new QueryWrapper();
        relationWrapper.eq("source_bid", addExpandVo.getSourceBid());
        List<MObject> existRelations = objectModelCrudI.listByQueryWrapper(addExpandVo.getRelationModelCode(), QueryWrapper.buildSqlQo(relationWrapper));
        List<MObject> relationAddList = Lists.<MObject>newArrayList();
        if (CollUtil.isNotEmpty(existRelations)) {
            // 5. 如果关系存在，则不重复添加
            List<String> existBidList = existRelations.stream().map(obj -> (String) obj.get("targetBid")).collect(Collectors.toList());
            List<MObject> list = objectModelCrudI.listByBids(existBidList, addExpandVo.getTargetModelCode());
            List<String> existPernos = list.stream().map(obj -> (String) obj.get("perno")).collect(Collectors.toList());
            existPernos.forEach(
                    existPerno -> existInstances.removeIf(instance -> {
                        String perno = (String) instance.get("perno");
                        return perno.equals(existPerno);
                    })
            );
            // 6. 组合待添加关系数据 （两部分数据 已存在的实例数据 + 新添加的实例数据）
            existInstances.forEach(instance -> buildRelationObject(addExpandVo, (String) instance.get(TranscendModelBaseFields.BID),  relationAddList));
            targetMObjects.forEach(targetMObject -> buildRelationObject(addExpandVo, (String) targetMObject.get(TranscendModelBaseFields.BID),  relationAddList));
        } else {
            // 5. 为空则直接组合待添加关系数据
            targetMObjects.forEach(targetMObject -> buildRelationObject(addExpandVo, (String) targetMObject.get(TranscendModelBaseFields.BID),  relationAddList));
        }
        // 7. 批量添加关系数据
        objectModelCrudI.addBatch(addExpandVo.getRelationModelCode(), relationAddList);
        return Boolean.TRUE;
    }

    private void initData(AddExpandAo addExpandVo, MSpaceAppData data) {
        data.setModelCode(addExpandVo.getTargetModelCode());
        data.put(TranscendModelBaseFields.MODEL_CODE, addExpandVo.getTargetModelCode());
        if (data.get(TranscendModelBaseFields.BID) != null) {
            data.put(TranscendModelBaseFields.DATA_BID, data.get(TranscendModelBaseFields.BID));
        } else {
            String bid = SnowflakeIdWorker.nextIdStr();
            data.put(TranscendModelBaseFields.DATA_BID, bid);
            data.put(TranscendModelBaseFields.BID, bid);
        }
        data.put(TranscendModelBaseFields.SPACE_BID, addExpandVo.getSpaceBid());
        data.put(TranscendModelBaseFields.SPACE_APP_BID, addExpandVo.getSpaceAppBid());
        ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(addExpandVo.getTargetModelCode())
                .getCheckExceptionData();
        objectModelCrudI.setMObjectDefaultValue(data, objectModelLifeCycleVO, addExpandVo.getTargetModelCode());
    }

    private void buildRelationObject(AddExpandAo addExpandVo, String targetBid, List<MObject> relationAddList) {
        MObject relation = new MObject();
        String bid = SnowflakeIdWorker.nextIdStr();
        relation.setBid(bid);
        relation.put(TranscendModelBaseFields.DATA_BID, bid);
        relation.put(TranscendModelBaseFields.SPACE_BID, addExpandVo.getSpaceBid());
        relation.put(TranscendModelBaseFields.SPACE_APP_BID, addExpandVo.getSpaceAppBid());
        relation.put(RelationObjectEnum.SOURCE_BID.getCode(), addExpandVo.getSourceBid());
        relation.put(RelationObjectEnum.TARGET_BID.getCode(), targetBid);
        relation.put(RelationObjectEnum.SOURCE_DATA_BID.getCode(), addExpandVo.getSourceBid());
        relation.put(RelationObjectEnum.TARGET_DATA_BID.getCode(), targetBid);
        relation.put(RelationObjectEnum.DRAFT.getCode(), false);
        relation.setModelCode(addExpandVo.getRelationModelCode());
        relation.setCreatedBy(SsoHelper.getJobNumber());
        relation.setUpdatedBy(SsoHelper.getJobNumber());
        relation.setUpdatedTime(LocalDateTime.now());
        relation.setCreatedTime(LocalDateTime.now());
        relation.setEnableFlag(true);
        relation.setDeleteFlag(false);
        relation.setTenantId(SsoHelper.getTenantId());
        relationAddList.add(relation);
    }

}
