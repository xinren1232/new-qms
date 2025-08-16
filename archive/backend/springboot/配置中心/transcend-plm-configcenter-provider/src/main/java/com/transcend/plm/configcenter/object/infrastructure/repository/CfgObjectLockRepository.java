package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.domain.entity.CfgObjectLock;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLockPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectLockMapper;
import com.transsion.framework.common.BeanUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ObjectLockRepository
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 11:34
 */
@Repository
public class CfgObjectLockRepository {

    @Resource
    private ObjectLockMapper objectLockMapper;

    /**
     * 删锁
     *
     * @param cfgObjectLock
     * @return
     * @version: 1.0
     * @date: 2022/11/25 13:40
     * @author: jingfang.luo
     */
    public Boolean deleteLock(CfgObjectLock cfgObjectLock) {
        CfgObjectLockPo po = BeanUtil.copy(cfgObjectLock, CfgObjectLockPo.class);
        return objectLockMapper.delete(po) > 0;
    }

    /**
     * 根据modelCodeList删除锁
     *
     * @param modelCodes
     * @return
     * @version: 1.0
     * @date: 2022/12/13 16:19
     * @author: jingfang.luo
     */
    public Boolean deleteByList(List<String> modelCodes) {
        return objectLockMapper.deleteByModelCodes(modelCodes) > 0;
    }

    /**
     * 上锁
     *
     * @param cfgObjectLock
     * @return
     * @version: 1.0
     * @date: 2022/11/25 13:40
     * @author: jingfang.luo
     */
    public Boolean lock(CfgObjectLock cfgObjectLock) {
        CfgObjectLockPo po = BeanUtil.copy(cfgObjectLock, CfgObjectLockPo.class);
        return objectLockMapper.insert(po) > 0;
    }

    /**
     * 查询所有锁
     *
     * @return
     * @version: 1.0
     * @date: 2022/11/25 13:40
     * @author: jingfang.luo
     */
    public List<CfgObjectLockPo> listAll() {
        return objectLockMapper.findAll();
    }

    /**
     * 根据modelCode查询对象链上的锁信息
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/11/25 13:40
     * @author: jingfang.luo
     */
    public List<CfgObjectLockPo> listInheritLockByModelCode(String modelCode) {
        if (StringUtils.isEmpty(modelCode)) {
            return Lists.newArrayList();
        }
        LinkedHashSet<String> modelCodes = ObjectCodeUtils.splitModelCode(modelCode);
        return objectLockMapper.findByModelCodeAndInModelCodes(modelCode, modelCodes);
    }

    /**
     * 根据modelCode和工号完全适配锁
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/7 10:01
     * @author: jingfang.luo
     */
    public CfgObjectLockPo listAdapterModelCode(String modelCode) {
        return objectLockMapper.findAdapterModelCode(modelCode, SsoHelper.getJobNumber());
    }

}
