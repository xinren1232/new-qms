package com.transcend.plm.configcenter.object.domain.aggergate;

import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.object.infrastructure.po.DomainObjectConfigPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.DomainObjectConfigRepository;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigAddParam;
import com.transcend.plm.configcenter.api.model.object.dto.DomainObjectConfigFindParam;
import com.transcend.plm.configcenter.api.model.object.vo.DomainObjectConfigVO;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * DomainObjectConfigRoot
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 11:36
 */
public class DomainObjectConfigRoot {

    @Resource
    private DomainObjectConfigRepository domainObjectConfigRepository = PlmContextHolder.getBean(DomainObjectConfigRepository.class);

    public static DomainObjectConfigRoot build() {
        return new DomainObjectConfigRoot();
    }

    /**
     * 批量新增
     */
    public Boolean bulkInsert(List<DomainObjectConfigAddParam> params) {
        if (CollectionUtil.isEmpty(params)) {
            return Boolean.TRUE;
        }
        List<DomainObjectConfigPo> configList = BeanUtil.copy(params, DomainObjectConfigPo.class);
        return domainObjectConfigRepository.bulkInsert(configList);
    }

    /**
     * 根据domainBid删除域对象配置
     */
    public Boolean deleteByDomainBid(String domainBid) {
        return domainObjectConfigRepository.deleteByDomainBid(domainBid);
    }

    public Boolean deleteByBid(String bid) {
        return domainObjectConfigRepository.deleteByBid(bid);
    }

    /**
     * 根据domainBid查询域对象配置
     */
    public List<DomainObjectConfigVO> findByDomainBid(String domainBid) {
        return BeanUtil.copy(
                domainObjectConfigRepository.findByDomainBid(domainBid),
                DomainObjectConfigVO.class
        );
    }

    public List<DomainObjectConfigVO> find(DomainObjectConfigFindParam domainObjectConfigFindParam) {
        return BeanUtil.copy(
                domainObjectConfigRepository.find(domainObjectConfigFindParam),
                DomainObjectConfigVO.class
        );
    }

}
