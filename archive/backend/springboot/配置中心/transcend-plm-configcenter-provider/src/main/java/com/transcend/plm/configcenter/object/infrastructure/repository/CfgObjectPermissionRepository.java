package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectPermissionMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectPermissionConverter;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectPermissionSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * CfgObjectPermissionRepository
 *
 * @author jie.luo1
 * @version: 1.0
 * @date 2023/02/18 10:45
 */
@Repository
public class CfgObjectPermissionRepository {
    @Resource
    private CfgObjectPermissionMapper cfgObjectPermissionMapper;
    @Resource
    private CfgObjectPermissionService cfgObjectPermissionService;

    public List<CfgObjectPermissionVo> listByModelCode(String modelCode) {
        return CfgObjectPermissionConverter.INSTANCE.pos2vos(
                cfgObjectPermissionMapper.listInModelCode(Sets.newHashSet(modelCode))
        );
    }

    public CfgObjectPermissionVo save(CfgObjectPermissionSaveParam saveParam) {
        CfgObjectPermissionPo po = CfgObjectPermissionConverter.INSTANCE.saveParam2po(saveParam);
        cfgObjectPermissionService.save(po);
        return CfgObjectPermissionConverter.INSTANCE.po2vo(po);
    }


    public CfgObjectPermissionVo updateByBid(CfgObjectPermissionSaveParam saveParam) {
        CfgObjectPermissionPo po = CfgObjectPermissionConverter.INSTANCE.saveParam2po(saveParam);
        return CfgObjectPermissionConverter.INSTANCE.po2vo(po);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgObjectPermissionService.logicalDeleteByBid(bid);
    }

    public Set<String> listBidByModelCode(String modelCode) {
        return cfgObjectPermissionMapper.listBidByModelCode(modelCode);
    }

    public Boolean logicalDeleteByBids(Set<String> bidSet) {
        if (CollectionUtils.isEmpty(bidSet)){
            return false;
        }
        return cfgObjectPermissionMapper.logicalDeleteByBids(bidSet) > 0;
    }


    public List<CfgObjectPermissionVo> listByModelCodes(Set<String> modelCodeDescSet) {
        return CfgObjectPermissionConverter.INSTANCE.pos2vos(
                cfgObjectPermissionMapper.listInModelCode(modelCodeDescSet)
        );
    }

    public List<CfgObjectPermissionVo> listByRoleCodeAndModelCode(String modelCode,List<String> roleCodes){
        return CfgObjectPermissionConverter.INSTANCE.pos2vos(
                cfgObjectPermissionMapper.listByRoleCodeAndModelCode(modelCode,roleCodes)
        );
    }
}
