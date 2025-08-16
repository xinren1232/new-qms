package com.transcend.plm.configcenter.object.domain.entity;

import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLockPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLockRepository;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectLockVO;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.ObjectUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ObjectLock
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 11:37
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgObjectLock {

    @Resource
    private CfgObjectLockRepository cfgObjectLockRepository = PlmContextHolder.getBean(CfgObjectLockRepository.class);
    @ApiModelProperty(value = "对象bid")
    private String objBid;
    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "检出对象名称")
    private String objectName;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

    @ApiModelProperty(value = "检出人名称")
    private String checkoutName;

    static CfgObjectLock buildWithLock(String objBid, String modelCode, String objectName) {
        CfgObjectLock cfgObjectLock = new CfgObjectLock();
        cfgObjectLock.setObjBid(objBid)
                .setModelCode(modelCode)
                .setObjectName(objectName)
                .setCheckoutBy(SsoHelper.getJobNumber())
                .setCheckoutName(SsoHelper.getName());
        return cfgObjectLock;
    }

    /**
     * 上锁
     */
    Boolean lock() {
        return cfgObjectLockRepository.lock(this);
    }

    /**
     * 删锁
     */
    Boolean deleteLock() {
        return cfgObjectLockRepository.deleteLock(this);
    }

    static CfgObjectLock build(String modelCode) {
        CfgObjectLock cfgObjectLock = new CfgObjectLock();
        cfgObjectLock.setModelCode(modelCode);
        return cfgObjectLock;
    }

    /**
     * 根据modelCode查询锁信息，查询出该对象整个链路的锁信息设置进去
     *
     * @return {@link ObjectLockVO}
     */
    ObjectLockVO listInheritLockByModelCode() {
        List<CfgObjectLockPo> po = cfgObjectLockRepository.listInheritLockByModelCode(getModelCode());
        String info = po.stream().map(lock ->
                "对象【" + lock.getObjectName() + "】被【" + lock.getCheckoutName() + "】检出")
                .collect(Collectors.joining(","));

        String jobNumber = po.stream().filter(lock -> ObjectUtil.equals(lock.getModelCode(), getModelCode()))
                .map(CfgObjectLockPo::getCheckoutBy).findFirst().orElse("");

        return ObjectLockVO.of().setLockInfo(info).setCheckoutBy(jobNumber);
    }


    /**
     * 根据modelCode和工号完全适配锁
     *
     * @return {@link CfgObjectLockPo}
     */
    CfgObjectLock findAdapterModelCode() {
        CfgObjectLockPo po = cfgObjectLockRepository.listAdapterModelCode(modelCode);
        if (ObjectUtil.isNotEmpty(po)) {
            return BeanUtil.copy(po, CfgObjectLock.class);
        }
        return null;
    }
}
