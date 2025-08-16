package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleAccess;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmRoleAccessMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleAccessService;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
* @author peng.qin
* @description 针对表【apm_role_access】的数据库操作Service实现
* @createDate 2023-09-20 16:15:29
*/
@Service
public class ApmRoleAccessServiceImpl extends ServiceImpl<ApmRoleAccessMapper, ApmRoleAccess>
    implements ApmRoleAccessService{
    @Override
    public List<ApmRoleAccess> getRoleAccessByRoleBidAndSphereBid(List<String> roleBidList, String sphereBid) {
        return list(Wrappers.<ApmRoleAccess>lambdaQuery()
                .in(CollUtil.isNotEmpty(roleBidList), ApmRoleAccess::getRoleBid, roleBidList)
                .eq(StrUtil.isNotBlank(sphereBid), ApmRoleAccess::getSphereBid, sphereBid)
                .eq(ApmRoleAccess::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .eq(ApmRoleAccess::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
    }


    @Override
    public List<ApmSpaceAppAccessVo> getAccessByRoleBidList(List<String> roleBidList) {
        if (CollUtil.isEmpty(roleBidList)) {
            throw new PlmBizException("角色ID列表为空");
        }
        return this.baseMapper.getAccessByRoleBidList(roleBidList);
    }

    @Override
    public List<ApmRoleAccess> listBySpaceAppBids(Collection spaceAppBids) {
        List<ApmRoleAccess> apmRoleAccesses = list(Wrappers.<ApmRoleAccess>lambdaQuery().in(ApmRoleAccess::getSpaceAppBid,spaceAppBids).eq(ApmRoleAccess::getDeleteFlag,false));
        return apmRoleAccesses;
    }
}




