package com.transcend.plm.configcenter.object.domain.aggergate;

import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLifeCycleRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ObjectLifecycleRoot
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/14 16:32
 */
public class CfgObjectLifecycleRoot {

    @Resource
    private CfgObjectLifeCycleRepository cfgObjectLifeCycleRepository = PlmContextHolder.getBean(CfgObjectLifeCycleRepository.class);

    @Resource
    private CfgObjectRepository cfgObjectRepository = PlmContextHolder.getBean(CfgObjectRepository.class);

    public static CfgObjectLifecycleRoot build() {
        return new CfgObjectLifecycleRoot();
    }

    /**
     * 生命周期模型行为 - 根据modelCodeList查询多个对象的生命周期（没有设置中文名称）
     */
    public Map<String, ObjectModelLifeCycleVO> findLifecycleByModelCodeList(List<String> modelCodeList) {
        if (CollectionUtil.isEmpty(modelCodeList)) {
            return Maps.newHashMap();
        }

        Map<String, ObjectModelLifeCycleVO> map = Maps.newHashMap();
        for (String modelCode : modelCodeList) {
            LinkedHashSet<String> splitCodeList = ObjectCodeUtils.splitModelCodeDesc(modelCode);
            this.splitCodeToGetLifeCycle(map, modelCode, splitCodeList);
        }

        return map;
    }

    /**
     * 生命周期模型行为 - 根据modelCodeList查询多个对象的生命周期（没有设置中文名称）（不拆分，直接in list）
     */
    public List<ObjectModelLifeCycleVO> findLifecycleInModelCodeList(List<String> modelCodeList) {
        List<CfgObjectLifeCyclePo> poList = cfgObjectLifeCycleRepository.findLifecycleInModelCodeList(modelCodeList);
        return BeanUtil.copy(poList, ObjectModelLifeCycleVO.class);
    }

    /**
     * 生命周期模型行为 - 根据objectBidList查询多个对象的生命周期（没有设置中文名称）
     */
    public Map<String, ObjectModelLifeCycleVO> findLifecycleByObjectBidList(List<String> objectBidList) {
        if (CollectionUtil.isEmpty(objectBidList)) {
            return Maps.newHashMap();
        }

        Map<String, String> bidAndModelCodeMap = cfgObjectRepository.findListByObjectBidList(objectBidList)
                .stream().collect(Collectors.toMap(CfgObjectPo::getBid, CfgObjectPo::getModelCode));

        Map<String, ObjectModelLifeCycleVO> map = Maps.newHashMap();
        for (String objectBid : objectBidList) {
            String modelCode = bidAndModelCodeMap.get(objectBid);
            LinkedHashSet<String> splitCodeList = ObjectCodeUtils.splitModelCodeDesc(modelCode);
            //从小到大遍历，有值就加入map并跳出
            this.splitCodeToGetLifeCycle(map, objectBid, splitCodeList);
        }

        return map;
    }

    /**
     * 从小到大遍历，有值就加入map并跳出
     */
    private void splitCodeToGetLifeCycle(Map<String, ObjectModelLifeCycleVO> map, String id,
                                         LinkedHashSet<String> splitCodeList) {
        //从小到大遍历，有值就加入map并跳出
        for (String code : splitCodeList) {
            CfgObjectLifeCyclePo po = cfgObjectLifeCycleRepository.find(code);
            if (ObjectUtils.isNotEmpty(po)) {
                //po不为空，赋值
                ObjectModelLifeCycleVO vo = BeanUtil.copy(po, ObjectModelLifeCycleVO.class);
                map.put(id, vo);
                break;
            }
        }
    }
}
