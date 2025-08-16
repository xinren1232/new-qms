package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCycle;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectModelLifeCycleMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.service.CfgObjectLifeCycleService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ObjectLifeCycleRepository
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/01 14:29
 */
@Repository
public class CfgObjectLifeCycleRepository {

    @Resource
    private ObjectModelLifeCycleMapper objectModelLifeCycleMapper;
    @Resource
    private CfgObjectLifeCycleService cfgObjectLifeCycleService;

    public CfgObjectLifeCyclePo find(String modelCode) {
        return objectModelLifeCycleMapper.find(modelCode);
    }

    public List<CfgObjectLifeCyclePo> list(LinkedHashSet<String> list) {
        return objectModelLifeCycleMapper.list(list);
    }

    public Boolean add(CfgObjectLifeCyclePo po) {
        return objectModelLifeCycleMapper.insert(po) > 0;
    }

    public Boolean update(CfgObjectLifeCyclePo po) {
        return objectModelLifeCycleMapper.update(po) > 0;
    }

    public Boolean delete(String modelCode) {
        return objectModelLifeCycleMapper.delete(modelCode) > 0;
    }

    public Boolean deleteByList(List<String> modelCodeList) {
        return objectModelLifeCycleMapper.deleteByModelCodeList(modelCodeList) > 0;
    }

    public List<CfgObjectLifeCyclePo> findLifecycleInModelCodeList(List<String> modelCodeList) {
        return objectModelLifeCycleMapper.findLifecycleInModelCodeList(modelCodeList);
    }
}
