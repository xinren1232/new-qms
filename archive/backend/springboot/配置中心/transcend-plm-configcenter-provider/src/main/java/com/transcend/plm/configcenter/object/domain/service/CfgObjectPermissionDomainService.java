package com.transcend.plm.configcenter.object.domain.service;

import com.google.common.collect.Sets;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectPermissionRepository;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectPermissionSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import com.transcend.plm.configcenter.role.domain.service.CfgRoleDomainService;
import com.transsion.framework.common.JsonUtil;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/3/14 9:41
 * @since 1.0
 */
@Service
public class CfgObjectPermissionDomainService {

    @Resource
    private CfgObjectPermissionRepository cfgObjectPermissionRepository;
    @Resource
    private CfgRoleDomainService cfgRoleDomainService;


    public List<CfgObjectPermissionVo> listByModelCode(String modelCode) {
        return cfgObjectPermissionRepository.listByModelCode(modelCode);
    }

    public CfgObjectPermissionVo saveOrUpdate(CfgObjectPermissionSaveParam cfgObjectPermissionSaveParam) {
        return StringUtil.isBlank(cfgObjectPermissionSaveParam.getBid()) ?
                cfgObjectPermissionRepository.save(cfgObjectPermissionSaveParam) :
                cfgObjectPermissionRepository.updateByBid(cfgObjectPermissionSaveParam);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgObjectPermissionRepository.logicalDeleteByBid(bid);
    }

    /**
     * 批量保存并修改
     * @param cfgObjectPermissionSaveParams
     * @return
     */
    public Boolean batchSaveOrUpdate(List<CfgObjectPermissionSaveParam> cfgObjectPermissionSaveParams) {
        cfgObjectPermissionSaveParams.forEach(cfgObjectPermissionSaveParam ->
                saveOrUpdate(cfgObjectPermissionSaveParam)
        );
        return true;
    }

    public Boolean batchCoverByModelCode(String modelCode, List<CfgObjectPermissionSaveParam> cfgObjectPermissionSaveParams) {
        // 1.过滤只能提交相同modelCode的数据，不能提交 非 modelCode的数据
        cfgObjectPermissionSaveParams = cfgObjectPermissionSaveParams.stream()
                .filter(saveParam -> modelCode.equals(saveParam.getModelCode()))
                .collect(Collectors.toList());
        // 2.获取绑定modelCode的权限
        Set<String> bidSet = cfgObjectPermissionRepository.listBidByModelCode(modelCode);
        // 3.收集需要的bid集合
        Set<String> handlerBidSet = cfgObjectPermissionSaveParams.stream()
                .map(CfgObjectPermissionSaveParam::getBid).collect(Collectors.toSet());
        // bid的差集，获取需要删除的bid集合
        if (bidSet.removeAll(handlerBidSet)){
           cfgObjectPermissionRepository.logicalDeleteByBids(bidSet);
        }
        // 4.收集需要新增的数据，收集需要更新的数据,分别处理 增删改
        batchSaveOrUpdate(cfgObjectPermissionSaveParams);
        return true;
    }


    public List<CfgObjectPermissionVo> listInheritByModelCode(String modelCode) {
        LinkedHashSet<String> modelCodeDescSet = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<CfgObjectPermissionVo> vos = cfgObjectPermissionRepository.listByModelCodes(modelCodeDescSet);
        vos.forEach(vo->{
            // 默认为false
            vo.setInherit(Boolean.FALSE);
            // 对象类型不匹配则为其父类
            if (!modelCode.equals(vo.getModelCode())){
                vo.setInherit(Boolean.TRUE);
            }
        });
        return vos;
    }

    /**
     * 获取当前登录人对象操作权限
     * @param modelCode
     * @return
     */
    public List<String> getOperationsByModelCode(String modelCode){
        String jobNumber = SsoHelper.getJobNumber();
        List<String> operations = new ArrayList<>();
        //通过jobNumber获取到所有角色信息
        List<String> roleCodes = cfgRoleDomainService.getRoleCodesByJobNumber(jobNumber);
        if(CollectionUtils.isEmpty(roleCodes) || StringUtil.isBlank(modelCode)){
           return operations;
        }
        List<CfgObjectPermissionVo> cfgObjectPermissionVos = cfgObjectPermissionRepository.listByRoleCodeAndModelCode(modelCode,roleCodes);
        for(CfgObjectPermissionVo cfgObjectPermissionVo:cfgObjectPermissionVos){
            roleCodes.addAll(cfgObjectPermissionVo.getOperations());
        }
        return roleCodes;
    }
}
