package com.transcend.plm.configcenter.role.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.common.constant.RoleConst;
import com.transcend.plm.configcenter.role.infrastructure.repository.mapper.CfgRoleUserMapper;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRoleUserPo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CfgRoleUserServiceImpl extends ServiceImpl<CfgRoleUserMapper, CfgRoleUserPo> implements CfgRoleUserService{
    @Override
    public Boolean removeByRoleBidAndJobNumber(String roleBid, String jobNumber) {
        if(StringUtils.isBlank(roleBid) || StringUtils.isBlank(jobNumber)){
            throw new IllegalArgumentException("roleBid or jobNumber is blank");
        }
        LambdaQueryWrapper<CfgRoleUserPo> wrapper = Wrappers.<CfgRoleUserPo>lambdaQuery().eq(CfgRoleUserPo::getRoleBid, roleBid).eq(CfgRoleUserPo::getJobNumber, jobNumber);
        return this.remove(wrapper);
    }

    @Override
    public List<CfgRoleUserPo> listByRoleCode(String roleCode) {
        if(StringUtils.isBlank(roleCode)){
            throw new IllegalArgumentException("roleCode is blank");
        }
        LambdaQueryWrapper<CfgRoleUserPo> wrapper = Wrappers.<CfgRoleUserPo>lambdaQuery().eq(CfgRoleUserPo::getRoleCode, roleCode);
        return this.list(wrapper);
    }

    @Override
    public List<String> listRoleCodesByJobNumber(String jobNumber) {
        if(StringUtils.isBlank(jobNumber)){
            throw new IllegalArgumentException("jobNumber is blank");
        }
        LambdaQueryWrapper<CfgRoleUserPo> wrapper = Wrappers.<CfgRoleUserPo>lambdaQuery().eq(CfgRoleUserPo::getJobNumber, jobNumber)
                .in(CfgRoleUserPo::getType, RoleConst.SYS_GLOBAL_ROLE_TYPE)
                .select(CfgRoleUserPo::getRoleCode);
        List<CfgRoleUserPo> roleUserPoList = this.list(wrapper);
        if (CollectionUtils.isEmpty(roleUserPoList)) {
            return Lists.newArrayList();
        }
        return roleUserPoList.stream().map(CfgRoleUserPo::getRoleCode).collect(Collectors.toList());
    }

    @Override
    public List<CfgRoleUserPo> listByRoleBidAndJobNumbers(String roleBid, List<String> jobNumbers) {
        if(StringUtils.isBlank(roleBid) || CollectionUtils.isEmpty(jobNumbers)){
            throw new IllegalArgumentException("roleBid or jobNumbers is blank");
        }
        LambdaQueryWrapper<CfgRoleUserPo> wrapper = Wrappers.<CfgRoleUserPo>lambdaQuery().eq(CfgRoleUserPo::getRoleBid, roleBid).in(CfgRoleUserPo::getJobNumber, jobNumbers);
        return this.list(wrapper);
    }

    @Override
    public Boolean removeByRoleCode(String roleCode) {
        if(StringUtils.isBlank(roleCode)){
            throw new IllegalArgumentException("roleCode is blank");
        }
        LambdaQueryWrapper<CfgRoleUserPo> wrapper = Wrappers.<CfgRoleUserPo>lambdaQuery().eq(CfgRoleUserPo::getRoleCode, roleCode);
        return this.remove(wrapper);
    }
}
