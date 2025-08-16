package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmAccessMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmAccessService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_access】的数据库操作Service实现
* @createDate 2023-09-20 16:15:29
*/
@Service
public class ApmAccessServiceImpl extends ServiceImpl<ApmAccessMapper, ApmAccess>
    implements ApmAccessService{

    @Override
    public List<ApmAccess> listByRoles(Set<String> roleBids, String sphereBid) {
        if (CollUtil.isEmpty(roleBids)) {
            return Lists.newArrayList();
        }
        return this.baseMapper.listByRoles(roleBids,sphereBid);
    }

    @Override
    public List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String sphereBid) {
        return this.baseMapper.queryAccessWithRoleBySphereBid(sphereBid);
    }

    @Override
    public boolean updateByBid(ApmAccess apmAccess) {
        LambdaUpdateWrapper<ApmAccess> updateWrapper = Wrappers.<ApmAccess>lambdaUpdate()
                .set(Objects.nonNull(apmAccess.getCode()), ApmAccess::getCode, apmAccess.getCode())
                .set(Objects.nonNull(apmAccess.getName()), ApmAccess::getName, apmAccess.getName())
                .set(Objects.nonNull(apmAccess.getResource()), ApmAccess::getResource, apmAccess.getResource())
                .set(Objects.nonNull(apmAccess.getType()), ApmAccess::getType, apmAccess.getType())
                .eq(ApmAccess::getBid, apmAccess.getBid());
        return this.baseMapper.update(apmAccess, updateWrapper) > 0;
    }

    @Override
    public List<ApmAccess> listByBids(Set<String> bids) {
        List<ApmAccess> list = this.baseMapper.listByBids(bids);
        return list;
    }

    @Override
    public List<ApmAccess> listByBidConllection(Collection bids) {
        List<ApmAccess> list = list(Wrappers.<ApmAccess>lambdaQuery().in(ApmAccess::getBid,bids));
        return list;
    }
}




