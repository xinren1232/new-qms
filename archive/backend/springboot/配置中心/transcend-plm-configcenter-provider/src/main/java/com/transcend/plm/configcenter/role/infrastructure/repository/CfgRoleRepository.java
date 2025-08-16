package com.transcend.plm.configcenter.role.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.common.constant.RoleConst;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ConfigCommonMapper;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRolePo;
import com.transcend.plm.configcenter.role.pojo.CfgRoleConverter;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CfgRoleRepository {
    @Resource
    private CfgRoleService cfgRoleService;
    @Resource
    private ConfigCommonMapper configCommonMapper;

    public CfgRoleVo save(CfgRoleDto cfgAttributeDto) {
        //判断编码是否重复
        int count = configCommonMapper.countByCondition("cfg_role","code",cfgAttributeDto.getCode());
        if(count > 0){
            //编码重复
            throw new BusinessException("编码重复");
        }
        Assert.notNull(cfgAttributeDto,"attribute is null");
        CfgRolePo cfgAttribute = CfgRoleConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgRoleService.save(cfgAttribute);
        return CfgRoleConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public boolean saveBatch(List<CfgRolePo> cfgRolePos){
        return cfgRoleService.saveBatch(cfgRolePos);
    }

    public CfgRoleVo update(CfgRoleDto cfgAttributeDto) {
        //判断编码是否重复
        List<CfgRolePo> cfgRolePos = cfgRoleService.queryByCodes(Collections.singletonList(cfgAttributeDto.getCode()));
        if(CollectionUtils.isNotEmpty(cfgRolePos)){
            List<CfgRolePo> collect = cfgRolePos.stream().filter(t -> !t.getBid().equals(cfgAttributeDto.getBid())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                //编码重复
                throw new BusinessException("编码重复");
            }
        }
        CfgRolePo cfgAttribute = CfgRoleConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgRoleService.updateByBid(cfgAttribute);
        return CfgRoleConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public CfgRoleVo getByBid(String bid) {
        CfgRolePo cfgAttribute =  cfgRoleService.getByBid(bid);
        return CfgRoleConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public PagedResult<CfgRoleVo> page(BaseRequest<CfgRoleQo> pageQo) {
        return cfgRoleService.pageByCfgAttributeQo(pageQo);
    }

    public List<CfgRoleVo> listAll() {
        LambdaQueryWrapper<CfgRolePo> queryWrapper = Wrappers.<CfgRolePo>lambdaQuery();
        List<CfgRolePo> roles = cfgRoleService.list(queryWrapper);
        return CfgRoleConverter.INSTANCE.pos2vos(roles);
    }

    public List<CfgRoleVo> queryByCodes(List<String> codeList) {
        List<CfgRolePo> cfgRolePos = cfgRoleService.queryByCodes(codeList);
        return CfgRoleConverter.INSTANCE.pos2vos(cfgRolePos);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgRoleService.logicalDeleteByBid(bid);
    }

    public List<CfgRoleVo> listGlobalRole() {
        LambdaQueryWrapper<CfgRolePo> queryWrapper = Wrappers.<CfgRolePo>lambdaQuery()
                .in(CfgRolePo::getType, RoleConst.SYS_GLOBAL_ROLE_TYPE)
                .eq(CfgRolePo::getDeleteFlag, 0);
        List<CfgRolePo> roles = cfgRoleService.list(queryWrapper);
        return CfgRoleConverter.INSTANCE.pos2vos(roles);
    }
}
