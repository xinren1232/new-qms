package com.transcend.plm.configcenter.objectrelation.domain.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.enums.CfgSysAppTabEnum;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectPathChainQO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.exception.PlmBizException;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.objectrelation.domain.service.IObjectRelationService;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.ObjectRelationRepository;
import com.transsion.framework.dto.BaseRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObjectRelationServiceImpl implements IObjectRelationService {

    @Resource
    private ObjectRelationRepository objectRelationRepository;

    @Resource
    private ICfgObjectAppService cfgObjectAppService;

    @Override
    public boolean add(CfgObjectRelationDto cfgObjectRelationDto) {
        return objectRelationRepository.add(cfgObjectRelationDto);
    }

    @Override
    public PagedResult<CfgObjectRelationVo> page(BaseRequest<CfgObjectRelationQo> pageQo) {
        return objectRelationRepository.page(pageQo);
    }

    @Override
    public boolean delete(String bid) {
        return objectRelationRepository.delete(bid);
    }

    @Override
    public boolean edit(CfgObjectRelationDto cfgObjectRelationDto) {
        return objectRelationRepository.edit(cfgObjectRelationDto);
    }

    @Override
    public boolean changeEnableFlag(String bid, Integer enableFlag) {
        return objectRelationRepository.changeEnableFlag(bid, enableFlag);
    }

    @Override
    public String getRelationRuleRes(ObjectRelationRuleQo objectRelationRuleQo) {
        return objectRelationRepository.getRelationRuleRes(objectRelationRuleQo);
    }

    @Override
    public List<CfgObjectRelationVo> listRelationTab(String modelCode) {
        //判断是否为blank，如果是则抛出异常
        Assert.hasText(modelCode, "modelCode is blank");
        //根据modelCode查询对象信息
        CfgObjectVo objectVo = cfgObjectAppService.getByModelCode(modelCode);
        //判断对象是否存在，如果不存在则抛出异常
        Assert.notNull(objectVo, "object is not exist");
        //拆分modelCode
        LinkedHashSet<String> codeSet = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<CfgObjectRelationVo> relationVos = objectRelationRepository.listRelationTab(Lists.newArrayList(codeSet));
        //创建3个CfgObjectRelationVo对象 tabName分别为版本记录、操作记录，评论功能；modelCode分别为100、101、102
        //对象类型是版本对象则增加版本记录
        if (ObjectTypeEnum.VERSION.getCode().equals(objectVo.getType())) {
            CfgObjectRelationVo cfgObjectRelationVo1 = new CfgObjectRelationVo();
            cfgObjectRelationVo1.setTabName(CfgSysAppTabEnum.VERSION_RECORD.getDescription());
            cfgObjectRelationVo1.setModelCode(CfgSysAppTabEnum.VERSION_RECORD.getModelCode());
            cfgObjectRelationVo1.setInner(true);
            relationVos.add(cfgObjectRelationVo1);
        }
        addInnerTabs(relationVos);
        return relationVos;
    }

    private static void addInnerTabs(List<CfgObjectRelationVo> relationVos) {
        CfgObjectRelationVo cfgObjectRelationVo2 = new CfgObjectRelationVo();
        cfgObjectRelationVo2.setTabName(CfgSysAppTabEnum.OPERATION_RECORD.getDescription());
        cfgObjectRelationVo2.setModelCode(CfgSysAppTabEnum.OPERATION_RECORD.getModelCode());
        cfgObjectRelationVo2.setInner(true);
        relationVos.add(cfgObjectRelationVo2);
        CfgObjectRelationVo cfgObjectRelationVo3 = new CfgObjectRelationVo();
        cfgObjectRelationVo3.setTabName(CfgSysAppTabEnum.COMMENT.getDescription());
        cfgObjectRelationVo3.setModelCode(CfgSysAppTabEnum.COMMENT.getModelCode());
        cfgObjectRelationVo3.setInner(true);
        relationVos.add(cfgObjectRelationVo3);
        CfgObjectRelationVo cfgObjectRelationVo4 = new CfgObjectRelationVo();
        cfgObjectRelationVo4.setTabName(CfgSysAppTabEnum.TEAM_MANAGEMENT.getDescription());
        cfgObjectRelationVo4.setModelCode(CfgSysAppTabEnum.TEAM_MANAGEMENT.getModelCode());
        cfgObjectRelationVo4.setInner(true);
        relationVos.add(cfgObjectRelationVo4);
        CfgObjectRelationVo cfgObjectRelationVo5 = new CfgObjectRelationVo();
        cfgObjectRelationVo5.setTabName(CfgSysAppTabEnum.RESOURCE_MANAGEMENT.getDescription());
        cfgObjectRelationVo5.setModelCode(CfgSysAppTabEnum.RESOURCE_MANAGEMENT.getModelCode());
        cfgObjectRelationVo5.setInner(true);
        relationVos.add(cfgObjectRelationVo5);
    }

    public List<CfgObjectRelationVo> listRelationByTargetModelCode(String modelCode) {
        //判断是否为blank，如果是则抛出异常
        Assert.hasText(modelCode, "modelCode is blank");
        //根据modelCode查询对象信息
        CfgObjectVo objectVo = cfgObjectAppService.getByModelCode(modelCode);
        //判断对象是否存在，如果不存在则抛出异常
        Assert.notNull(objectVo, "object is not exist");
        //拆分modelCode
        LinkedHashSet<String> codeSet = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        return objectRelationRepository.listRelationByTargetModelCode(Lists.newArrayList(codeSet));
    }

    @Override
    public List<CfgObjectRelationVo> listRelationBySTModelCode(String sourceModelCode, String targetModelCode) {
        LinkedHashSet<String> sourceModelCodes = ObjectCodeUtils.splitModelCodeDesc(sourceModelCode);
        LinkedHashSet<String> targetModelCodes = ObjectCodeUtils.splitModelCodeDesc(targetModelCode);
        return objectRelationRepository.listRelationBySTModelCode(Lists.newArrayList(sourceModelCodes), Lists.newArrayList(targetModelCodes));
    }

    /**
     * 以源和目标对象查看跨层级关系列表
     * 如：源对象为A，目标对象为D，中间有B、C两个对象，A->B->C->D 那么查询的关系列表为A->B、A->C、A->D
     * 如果也存在 A->B->D 那么查询的关系列表为A->B、A->D
     *
     * @param sourceModelCode 源对象
     * @param targetModelCode 目标对象
     */
    @Override
    public Map<String, List<CfgObjectRelationVo>> listGroupCrossRelationBySTModelCode(String sourceModelCode, String targetModelCode, Set<String> conditionModelCodes) {
        Map<String, List<CfgObjectRelationVo>> result = Maps.newHashMap();
        /**
         * todo暂时不处理穿透的子对象的情况
         * 场景：目标A（任务）  找   源D（项目）
         * 查询策略：
         * 第一次查询：
         * 目标对象 A->AB（任务-迭代）
         *           AC（任务-版本）
         *           AK（任务-其他对象）
         * 第二次查询（收集上述结构的目标对象，以及不能等于 A,D对象（收集到去重列表中，因为后边的C也查询过，第三次就不用查了））：
         * 目标对象 B,C,K->BC（迭代-版本）
         *               BD（迭代-项目） 结束寻址
         *               CD（版本-项目） 结束寻址
         *               KE（其他对象K-其他对象E）
         * 第三次查询：
         * 目标对象(重复，) C-> CD（版本-项目） 结束寻址
         * 目标对象(不重复) E-> EF（其他对象E-其他对象F）
         *
         */
        // 暂时只查询三次关系，因为超过四层关系的查询效率太低（目前需求最远的需求是 任务->项目， 中间链路：任务->迭代，迭代->版本，版本->项目）
        // todo 暂时不处理穿透的子对象的情况
        LinkedHashSet<String> sourceModelCodes = ObjectCodeUtils.splitModelCodeDesc(sourceModelCode);
        LinkedHashSet<String> targetModelCodes = ObjectCodeUtils.splitModelCodeDesc(targetModelCode);
        // 已使用过的，后续不用查询
        Set<String> usedModelCodes = new HashSet<>();
        usedModelCodes.add(sourceModelCode);
        usedModelCodes.add(targetModelCode);
        // 用于存储查询到的目标对象
        Map<String, List<CfgObjectRelationVo>> targetModelCodeMap = Maps.newHashMap();
        targetModelCodeMap.put(targetModelCode, Lists.newArrayList());
        // 第一次查找目标向上一层
        collectCrossRelations(sourceModelCode, targetModelCodeMap, result, usedModelCodes, conditionModelCodes, 1, 4);
        // 转换结果集
        return result;
    }

    @Override
    public Map<String, CfgObjectRelationVo> listCrossRelationByPathChain(List<ObjectPathChainQO> qos) {
        if (CollectionUtils.isEmpty(qos)) {
            return new HashMap<>();
        }
        Map<String, CfgObjectRelationVo> relationVoMap = new HashMap<>();
        for (int i = 0; i < qos.size() - 1 ; i++) {
            ObjectPathChainQO beforePathQo = qos.get(i);
            ObjectPathChainQO afterPathQo = qos.get(i + 1);
            String beforeModelCode = beforePathQo.getModelCode();
            String afterModelCode = afterPathQo.getModelCode();
            String direction = afterPathQo.getDirection();
            String key = beforeModelCode + "->" + afterModelCode;
            String targetModelCode = "up".equals(direction) ? beforeModelCode : afterModelCode;
            String sourceModelCode = "down".equals(direction) ? beforeModelCode : afterModelCode;
            CfgObjectRelationQo relationQo = new CfgObjectRelationQo();
            relationQo.setSourceModelCode(sourceModelCode);
            relationQo.setTargetModelCode(targetModelCode);
            relationQo.setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
            relationQo.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
            List<CfgObjectRelationVo> relationVos = objectRelationRepository.list(relationQo);
            if (CollectionUtils.isNotEmpty(relationVos)) {
                relationVoMap.put(key, relationVos.get(0));
            }
        }
        return relationVoMap;
    }

    @NotNull
    private void collectCrossRelations(String sourceModelCode, Map<String, List<CfgObjectRelationVo>> targetModelCodeMap,
                                       Map<String, List<CfgObjectRelationVo>> result, Set<String> usedModelCodes,
                                       Set<String> conditionModelCodes,
                                       int level, int maxLevel) {
        // 穿透
//        LinkedHashSet<String> sourceModelCodes = ObjectCodeUtils.splitModelCodeDesc(sourceModelCode);
//        LinkedHashSet<String> targetModelCodes = ObjectCodeUtils.splitModelCodeDesc(targetModelCode);
        Map<String, List<CfgObjectRelationVo>> preResult1 = Maps.newHashMap();
        List<CfgObjectRelationVo> cfgObjectRelationVos = objectRelationRepository
                .listRelationByTargetModelCode(Lists.newArrayList(targetModelCodeMap.keySet()));
        if (CollectionUtils.isNotEmpty(cfgObjectRelationVos)) {

            Map<String, List<CfgObjectRelationVo>> sourceModelCodes1 = cfgObjectRelationVos.stream()
                    .collect(Collectors.groupingBy(CfgObjectRelationVo::getSourceModelCode));
            // 遍历处理
            sourceModelCodes1.forEach((k, v) -> {
                // 匹配最终寻找的目的就是源对象，如果存在了，则把关系对象收集到结果集中
                if (sourceModelCode.equals(k)) {
                    v.forEach(
                            cfgObjectRelationVo -> {
                                StringBuilder keysss = new StringBuilder();
                                // 拼接的key
                                List<CfgObjectRelationVo> cfgObjectRelationVos1 = targetModelCodeMap.get(cfgObjectRelationVo.getTargetModelCode());
                                // 不存在直接，则是第一次
                                if (CollectionUtils.isEmpty(cfgObjectRelationVos1))   {
                                    result.put(keysss.append(cfgObjectRelationVo.getModelCode()).toString(), v);
                                    return;
                                }
                                List<String> keySp = cfgObjectRelationVos1
                                        .stream()
                                        .map(CfgObjectRelationVo::getModelCode)
                                        .collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(keySp)) {
                                    keySp.forEach(key -> {
                                        keysss.append(key).append("#");
                                    });
                                }
                                // 复制一份，防止修改原来的数据
                                List<CfgObjectRelationVo> copyRelationVos = new ArrayList<>(cfgObjectRelationVos1);
                                copyRelationVos.add(cfgObjectRelationVo);
                                // key为关系的modelCode
                                result.put(keysss.append(cfgObjectRelationVo.getModelCode()).toString(), copyRelationVos);
                                usedModelCodes.add(cfgObjectRelationVo.getTargetModelCode());
                            }
                    );

                } else {
                    // 如果不存在，则继续向上查找（）
                    v.forEach(
                            cfgObjectRelationVo -> {
                                List<CfgObjectRelationVo> cfgObjectRelationVos1 = targetModelCodeMap.get(cfgObjectRelationVo.getTargetModelCode());
                                // 需要复制，因为后边会追加，如果不复制，会导致后边的数据被覆盖

                                List<CfgObjectRelationVo> cfgObjectRelationVos2 = preResult1.get(k);
                                if (CollectionUtils.isEmpty(cfgObjectRelationVos2)) {
                                    List<CfgObjectRelationVo> copy = new ArrayList<>(cfgObjectRelationVos1);
                                    copy.add(cfgObjectRelationVo);
                                    preResult1.put(k, copy);
                                    return;
                                }
//                                cfgObjectRelationVos2.add(cfgObjectRelationVo);
                            });
                }
            });
            preResult1.remove(usedModelCodes);
            // 移除 包含条件得modelCode
            if (CollectionUtils.isNotEmpty(conditionModelCodes)){
                preResult1.keySet().retainAll(conditionModelCodes);
            }
        }
        if (level < maxLevel && !CollectionUtils.isEmpty(preResult1)) {
            collectCrossRelations(sourceModelCode, preResult1, result, usedModelCodes, conditionModelCodes, ++level, maxLevel);
        }
    }

    @Override
    public CfgObjectRelationVo getRelationByModelCode(String modelCode) {
        if (StringUtil.isBlank(modelCode)) {
            throw new PlmBizException("modelCode is blank");
        }
        return objectRelationRepository.getByModelCode(modelCode);
    }
}
