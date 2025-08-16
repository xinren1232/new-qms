package com.transcend.plm.configcenter.object.domain.aggergate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectAttributeRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.ObjectUtil;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 对象属性聚合根
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/05 18:30
 */
@Slf4j
public class CfgObjectAttrRoot {

    @Resource
    private CfgObjectAttributeRepository cfgObjectAttributeRepository = PlmContextHolder.getBean(CfgObjectAttributeRepository.class);

    @Resource
    private CfgObjectRepository cfgObjectRepository = PlmContextHolder.getBean(CfgObjectRepository.class);

    @Resource
    private TransactionTemplate transactionTemplate = PlmContextHolder.getBean(TransactionTemplate.class);

    public static CfgObjectAttrRoot build() {
        return new CfgObjectAttrRoot();
    }

    public List<CfgObjectAttributeVo> findByModelCode(String modelCode) {
        //根据modelCode查询所有属性
        LinkedHashSet<String> selfAndSuperModelCodeSet = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<CfgObjectAttributePo> poList = cfgObjectAttributeRepository.findInModelCodeList(Lists.newArrayList(selfAndSuperModelCodeSet));
        if (CollectionUtil.isEmpty(poList)) {
            return Lists.newArrayList();
        }
        List<CfgObjectAttributeVo> resultAttrVos = Lists.newArrayList();

        //将对象属性分成两个组，一个是当前类的属性，一个是父类的属性
        Map<String, List<CfgObjectAttributeVo>> attrGroup = poList.stream()
                .map(po -> BeanUtil.copy(po, CfgObjectAttributeVo.class))
                .collect(Collectors.groupingBy(cfgObjectAttributeVo -> modelCode.equals(cfgObjectAttributeVo.getModelCode()) ? "self" : "super"));
        List<CfgObjectAttributeVo> selfAttrVos = attrGroup.get("self");
        Set<String> selfAttrCodeSet = Sets.newHashSet();
        if (CollectionUtil.isNotEmpty(selfAttrVos)) {
            resultAttrVos.addAll(selfAttrVos);
            selfAttrCodeSet = selfAttrVos.stream().map(CfgObjectAttributeVo::getCode).collect(Collectors.toSet());
        }

        //像当前类的属性中填充父类的属性
        List<CfgObjectAttributeVo> superAttrVos = attrGroup.get("super");
        if (CollectionUtil.isNotEmpty(superAttrVos)) {
            for (CfgObjectAttributeVo superAttrVo : superAttrVos) {
                if (!selfAttrCodeSet.contains(superAttrVo.getCode())) {
                    superAttrVo.setInherit(Boolean.TRUE);
                    resultAttrVos.add(superAttrVo);
                }
            }
        }

        //排序 -->
        return resultAttrVos.stream().sorted(
                Comparator.comparing(CfgObjectAttributeVo::getSort)
                        .thenComparing((CfgObjectAttributeVo attr) -> Boolean.TRUE.equals(attr.getInherit()) ? 1 : 2)
        ).collect(Collectors.toList());
    }


    public List<CfgObjectAttributeVo> findChildrenByModelCode(String modelCode) {
        //只有是基类的时候才获取属性
        if (ObjectCodeUtils.isBaseModel(modelCode)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "请传入基类对象的bid");
        }

        List<CfgObjectAttributePo> poList = cfgObjectAttributeRepository.findLikeModelCode(modelCode);

        if (CollectionUtil.isEmpty(poList)) {
            return Lists.newArrayList();
        }

        List<CfgObjectAttributeVo> voList = BeanUtil.copy(poList, CfgObjectAttributeVo.class);

        return voList.stream().sorted(
                //继承的放前面
                Comparator.comparing((CfgObjectAttributeVo attr) -> Boolean.FALSE.equals(attr.getCustom()) ? 1 : 2)
                        .thenComparing(CfgObjectAttributeVo::getSort)
        ).collect(Collectors.toList());
    }

    public Boolean checkin(CfgObject cfgObject) {
        String objBid = cfgObject.getBid();
        String modelCode = cfgObject.getModelCode();
        String modelVersionCode = cfgObject.getModelVersionCode();
        Integer version = cfgObject.getVersion();
        List<CfgObjectAttributeVo> attrList = cfgObject.getAttrList();

        return transactionTemplate.execute(s -> {
            //根据modelCode 查询原来的属性数据
            List<String> modelCodes = new ArrayList<>();
            modelCodes.add(modelCode);
            List<CfgObjectAttributePo> oldAttrList = cfgObjectAttributeRepository.findInModelCodeList(modelCodes);
            //将oldAttrList组装成Map数据，key是code,value是published,published为空的不需要
            Map<String, Boolean> oldAttrMap = new HashMap<>();
            for(CfgObjectAttributePo po : oldAttrList){
                oldAttrMap.put(po.getCode(), po.getPublished());
                /*if(po.getPublished() != null){
                    oldAttrMap.put(po.getCode(), po.getPublished());
                }*/
            }
            List<String> excludeAttrCodeList = new ArrayList<>();
            //attrList中code在oldAttrMap中存在的，需要排除，并在attrList删除
            for(int i=attrList.size()-1;i >= 0;i--){
                CfgObjectAttributeVo vo = attrList.get(i);
                if(oldAttrMap.containsKey(vo.getCode())){
                    excludeAttrCodeList.add(vo.getCode());
                    attrList.remove(i);
                }
            }
            //删除新表属性
            Boolean deleteAttr = cfgObjectAttributeRepository.deleteExcludeCodes(modelCode,excludeAttrCodeList);

            if (CollectionUtil.isEmpty(attrList)) {
                log.info("执行属性检入操作，删除属性结果：{}；无属性不执行插入属性操作", deleteAttr);
                return true;
            }

            //收集更新数据
            List<CfgObjectAttributeVo> voList = this.collectUpdateList(objBid, modelCode, attrList);

            if (CollectionUtil.isEmpty(voList)) {
                return true;
            }
            AtomicInteger i = new AtomicInteger();
            List<CfgObjectAttributePo> poList = voList.stream()
                    .map(attr -> {
                        CfgObjectAttributePo po = BeanUtil.copy(attr, CfgObjectAttributePo.class);
                        //把对象的版本设置到属性中
                        po.setModelVersionCode(modelVersionCode).setObjVersion(version);
                        //通过oldAttrMap设置发布状态
                        po.setPublished(oldAttrMap.get(attr.getCode()));
                        po.setSort(i.getAndIncrement());
                        return po;
                    })
                    .collect(Collectors.toList());

            //插入数据
            Boolean addAttr = cfgObjectAttributeRepository.bulkAdd(poList);

            log.info("执行属性检入操作，删除属性结果：{}；插入属性结果：{}", deleteAttr, addAttr);

            return true;
        });
    }

    /**
     * 收集更新数据
     */
    private List<CfgObjectAttributeVo> collectUpdateList(String objBid, String modelCode, List<CfgObjectAttributeVo> attrList) {
        //自身的属性(非继承的)
        List<CfgObjectAttributeVo> selfList = attrList.stream()
                .filter(attr -> ObjectUtil.equals(attr.getModelCode(), modelCode)
                        && ObjectUtil.equals(attr.getInherit(), Boolean.FALSE))
                .collect(Collectors.toList());

        //新增的属性（需要校验innerName和explain基类唯一）
        List<CfgObjectAttributeVo> addList = attrList.stream()
                .filter(attr -> StringUtil.isEmpty(attr.getModelCode()))
                .collect(Collectors.toList());
        addList.forEach(vo -> vo.setModelCode(modelCode).setObjBid(objBid)
                // TODO: 2023/1/5 新加属性设置dbKey，暂时把sort设置到dbKey中，后续dbKey会做改造
                .setDbKey(String.valueOf(vo.getSort())).setBid(SnowflakeIdWorker.nextIdStr()));

        //由于校验基类唯一的时候findLikeModelCode不知道什么原因like不出自身属性，现在暂时强行校验自身属性innerName和explain基类唯一
        String selfCheckInfo = this.checkSelfUnique(selfList, addList);

        //对新增的属性进行校验innerName和explain基类唯一
        String baseCheckInfo = this.checkBaseClassUnique(modelCode, addList);

        String info = selfCheckInfo + baseCheckInfo;
        if (StringUtil.isNotEmpty(info)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), info);
        }

        //继承自定义属性
        List<CfgObjectAttributeVo> customList = attrList.stream()
                .filter(attr -> ObjectUtil.equals(attr.getCustom(), Boolean.TRUE)
                        && ObjectUtil.equals(attr.getInherit(), Boolean.TRUE))
                .collect(Collectors.toList());
        customList.forEach(attr -> attr.setModelCode(modelCode).setBid(SnowflakeIdWorker.nextIdStr()));

        List<CfgObjectAttributeVo> voList = Lists.newArrayList();
        voList.addAll(selfList);
        voList.addAll(addList);
        voList.addAll(customList);
        return voList;
    }

    /**
     * 校验自身属性innerName和explain基类唯一
     */
    private String checkSelfUnique(List<CfgObjectAttributeVo> selfList, List<CfgObjectAttributeVo> addList) {
        List<String> codeList = selfList.stream().map(CfgObjectAttributeVo::getCode).collect(Collectors.toList());
        List<String> nameList = selfList.stream().map(CfgObjectAttributeVo::getName).collect(Collectors.toList());
        List<String> repeatInnerNameList = addList.stream()
                .filter(attr -> codeList.contains(attr.getCode()))
                .map(CfgObjectAttributeVo::getCode)
                .collect(Collectors.toList());
        List<String> repeatExplainList = addList.stream()
                .filter(attr -> nameList.contains(attr.getName()))
                .map(CfgObjectAttributeVo::getName)
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();

        if (CollectionUtil.isNotEmpty(repeatInnerNameList)) {
            for (String repeatInnerName : repeatInnerNameList) {
                String innerNameInfo = "对象自身已存在属性名称为【" + repeatInnerName + "】的属性；";
                sb.append(innerNameInfo);
            }
        }

        if (CollectionUtil.isNotEmpty(repeatExplainList)) {
            for (String repeatExplain : repeatExplainList) {
                String innerNameInfo = "对象自身已存在显示名称为【" + repeatExplain + "】的属性；";
                sb.append(innerNameInfo);
            }
        }

        return sb.toString();
    }

    /**
     * 校验基类innerName和explain唯一
     */
    private String checkBaseClassUnique(String modelCode, List<CfgObjectAttributeVo> addList) {
        //获取基类modelCode
        String baseModelCode = modelCode.substring(0, 3);
        List<CfgObjectAttributePo> allAttrList = cfgObjectAttributeRepository.findLikeModelCode(baseModelCode);
        Map<String, String> attrCodeAndModelCodeMap = allAttrList.stream()
                .collect(Collectors.toMap(CfgObjectAttributePo::getCode,
                        CfgObjectAttributePo::getModelCode, ((m1, m2) -> m1.length() < m2.length() ? m1 : m2)));
        Map<String, String> explainAndModelCodeMap = allAttrList.stream()
                .collect(Collectors.toMap(CfgObjectAttributePo::getName,
                        CfgObjectAttributePo::getModelCode, ((m1, m2) -> m1.length() < m2.length() ? m1 : m2)));

        Map<String, String> modelCodeAndInnerNameCheckMap = Maps.newHashMap();
        Map<String, String> modelCodeAndExplainCheckMap = Maps.newHashMap();

        for (CfgObjectAttributeVo vo : addList) {
            String innerName = vo.getCode();
            if (attrCodeAndModelCodeMap.containsKey(innerName)) {
                modelCodeAndInnerNameCheckMap.put(attrCodeAndModelCodeMap.get(innerName), innerName);
            }
            String explain = vo.getName();
            if (explainAndModelCodeMap.containsKey(explain)) {
                modelCodeAndExplainCheckMap.put(explainAndModelCodeMap.get(explain), explain);
            }
        }

        List<String> modelCodeList = Lists.newArrayList();
        modelCodeList.addAll(modelCodeAndInnerNameCheckMap.keySet());
        modelCodeList.addAll(modelCodeAndExplainCheckMap.keySet());

        if (CollectionUtil.isEmpty(modelCodeList)) {
            return "";
        }

        List<CfgObjectPo> objectModelList = cfgObjectRepository.findListByModelCodeList(modelCodeList);

        Map<String, String> objModeCodeAndObjNameMap = objectModelList.stream()
                .collect(Collectors.toMap(CfgObjectPo::getModelCode, CfgObjectPo::getName));

        StringBuilder sb = new StringBuilder();

        if (ObjectUtil.isNotEmpty(modelCodeAndInnerNameCheckMap)) {
            modelCodeAndInnerNameCheckMap.forEach((code, name) -> {
                String innerNameInfo = "对象【" + objModeCodeAndObjNameMap.get(code) + "】已存在属性名称为【" + name + "】的属性；";
                sb.append(innerNameInfo);
            });
        }

        if (ObjectUtil.isNotEmpty(modelCodeAndExplainCheckMap)) {
            modelCodeAndExplainCheckMap.forEach((code, ex) -> {
                String attrCodeInfo = "对象【" + objModeCodeAndObjNameMap.get(code) + "】已存在显示名称为【" + ex + "】的属性；";
                sb.append(attrCodeInfo);
            });
        }

        return sb.toString();
    }

    public Map<String, List<CfgObjectAttributeVo>> findAttrsByModelCodeList(List<String> modelCodeList) {
        Map<String, List<CfgObjectAttributeVo>> map = Maps.newHashMap();
        for (String code : modelCodeList) {
            List<CfgObjectAttributeVo> attrs = this.findByModelCode(code);
            map.put(code, attrs);
        }
        return map;
    }

    public List<CfgObjectAttributeVo> findHistoryByModelCode(String modelCode, Integer version) {
        // 查询当前modelCode以及父modelCode列表，从子->父排列
        LinkedHashSet<String> codeList = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<String> modelVersionCodeList = Lists.newArrayList();
        modelVersionCodeList.add(modelCode + ":" + version);
        final Integer[] modelVersion = {version};
        codeList.forEach(code -> {
            CfgObjectPo history = cfgObjectRepository.findHistory(code, modelVersion[0]);
            if (history == null || "0:1".equals(history.getParentModelVersionCode())) {
                return;
            }
            String parentModelVersionCode = history.getParentModelVersionCode();
            if (StringUtil.isNotBlank(parentModelVersionCode)) {
                modelVersionCodeList.add(parentModelVersionCode);
                modelVersion[0] = Integer.parseInt(parentModelVersionCode.split(":")[1]);
            }
        });
        List<CfgObjectAttributePo> poList = cfgObjectAttributeRepository.findHistoryByModelVersionCodeList(modelVersionCodeList);

        if (CollectionUtil.isEmpty(poList)) {
            return Lists.newArrayList();
        }

        Map<String, List<CfgObjectAttributeVo>> map = poList.stream()
                .map(po -> BeanUtil.copy(po, CfgObjectAttributeVo.class))
                .collect(Collectors.groupingBy(CfgObjectAttributeVo::getModelCode));

        //有自定义的innerNameList
        List<String> customInnerNameList = poList.stream()
                .collect(Collectors.groupingBy(CfgObjectAttributePo::getCode))
                .entrySet().stream().filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<CfgObjectAttributeVo> voList = Lists.newArrayList();
        Set<String> innerNameSet = Sets.newHashSet();

        //从小到大遍历，有值就不放进去
        for (String code : codeList) {
            List<CfgObjectAttributeVo> attrList = map.get(code);
            if (CollectionUtil.isEmpty(attrList)) {
                continue;
            }
            for (CfgObjectAttributeVo attribute : attrList) {
                String innerName = attribute.getCode();
                if (!innerNameSet.contains(innerName)) {
                    innerNameSet.add(innerName);
                    attribute.setInherit(!ObjectUtil.equals(code, modelCode));
                    attribute.setCustom(ObjectUtil.equals(code, modelCode));
                    voList.add(attribute);
                }
                //存在自定义的属性还是设置为继承
                if (customInnerNameList.contains(innerName)) {
                    attribute.setInherit(Boolean.TRUE);
                }
            }
        }

        //排序 --> 基础属性 -> 继承属性 -> 自定义属性 -> 自身顺序
        return voList.stream().sorted(
                Comparator.comparing((CfgObjectAttributeVo attr) -> Boolean.TRUE.equals(attr.getBaseAttr()) ? 1 : 2)
                        .thenComparing((CfgObjectAttributeVo attr) -> Boolean.TRUE.equals(attr.getInherit()) ? 1 : 2)
                        .thenComparing((CfgObjectAttributeVo attr) -> Boolean.TRUE.equals(attr.getCustom()) ? 1 : 2)
                        .thenComparing(CfgObjectAttributeVo::getSort)
        ).collect(Collectors.toList());
    }

}
