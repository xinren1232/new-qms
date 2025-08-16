package com.transcend.plm.configcenter.object.domain.aggergate;

import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.object.domain.entity.CfgObjectLock;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLockPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLockRepository;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 对象锁聚合根
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 13:52
 */
public class CfgObjectLockRoot {

    @Resource
    private CfgObjectLockRepository cfgObjectLockRepository = PlmContextHolder.getBean(CfgObjectLockRepository.class);

    public static CfgObjectLockRoot build() {
        return new CfgObjectLockRoot();
    }

    /**
     * 填充锁信息
     */
    List<CfgObjectTreeVo> populateLockInfo(List<CfgObjectTreeVo> objectModelList) {
        //查询出所有锁信息
        List<CfgObjectLockPo> poList = cfgObjectLockRepository.listAll();
        Map<String, String> lockInfoMap = this.lockInfoMap(poList);
        Map<String, String> checkoutByMap = poList.stream().collect(Collectors.toMap(CfgObjectLockPo::getModelCode, CfgObjectLockPo::getCheckoutBy));

        //遍历对象填充锁信息
        for (CfgObjectTreeVo treeVO : objectModelList) {
            String modelCode = treeVO.getModelCode();
            StringJoiner sj = new StringJoiner(",");
            lockInfoMap.forEach((modelCodeLock, lockInfo) -> {
                if (modelCode.contains(modelCodeLock) || modelCodeLock.contains(modelCode)) {
                    sj.add(lockInfo);
                }
            });
            treeVO.setLockInfo(sj.toString());
            if (checkoutByMap.containsKey(modelCode)) {
                treeVO.setCheckoutBy(checkoutByMap.get(modelCode));
            }
        }
        return objectModelList;
    }

    /**
     * 查询所有锁信息Map<modelCode,lockInfo>
     */
    private Map<String, String> lockInfoMap(List<CfgObjectLockPo> poList) {
        List<CfgObjectLock> all = BeanUtil.copy(poList, CfgObjectLock.class);

        if (CollectionUtil.isEmpty(all)) {
            return Maps.newHashMap();
        }
        return all.stream().collect(Collectors.toMap(CfgObjectLock::getModelCode, lock ->
                "对象【" + lock.getObjectName() + "】被【" + lock.getCheckoutName() + "】检出"
        ));
    }

    /**
     * 根据modelCode查询锁信息，查询出该对象整个链路的锁信息
     *
     * @param modelCode
     * @return {@link String}
     */
    public String listAllChainLockInfoByModelCode(String modelCode) {

        List<CfgObjectLockPo> poList = cfgObjectLockRepository.listInheritLockByModelCode(modelCode);
        Map<String, String> map = this.lockInfoMap(poList);
        if (CollectionUtil.isEmpty(map)) {
            return null;
        }
        return String.join(",", map.values());
    }

}
