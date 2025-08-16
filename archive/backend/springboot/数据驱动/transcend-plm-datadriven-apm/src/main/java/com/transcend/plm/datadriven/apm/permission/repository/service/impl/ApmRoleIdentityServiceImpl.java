package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleIdentityConerter;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmRoleIdentityMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.transcend.plm.datadriven.common.constant.CommonConst.*;

/**
* @author peng.qin
* @description 针对表【apm_role_identity】的数据库操作Service实现
* @createDate 2023-09-20 16:15:30
*/
@Service
public class ApmRoleIdentityServiceImpl extends ServiceImpl<ApmRoleIdentityMapper, ApmRoleIdentity>
    implements ApmRoleIdentityService{

    @Resource
    private ApmRoleIdentityMapper apmRoleIdentityMapper;

    @Override
    public List<ApmRoleIdentity> listByRoleBids(List<String> roleBids) {
        return list(Wrappers.<ApmRoleIdentity>lambdaQuery().in(ApmRoleIdentity::getRoleBid,roleBids).orderBy(true,true,ApmRoleIdentity::getSort));
    }

    @Override
    public List<ApmRoleIdentity> listAllBySphereBid(String sphereBid, String type, String name) {
        return this.getBaseMapper().listAllBySphereBid(sphereBid, type, name);
    }

    @Override
    public List<ApmRoleIdentity> listAllRoleAndIdentityBySphereBid(String sphereBid) {
        return this.getBaseMapper().listAllRoleAndIdentityBySphereBid(sphereBid);
    }

    @Override
    public List<ApmRoleIdentity> listInputPercentageNotNull(Set<String> apmSphereBids) {
        return this.getBaseMapper().listInputPercentageNotNull(apmSphereBids);
    }

    @Override
    public ApmRoleIdentityVO getApmRoleIdentityVOByForeignBid(String foreignBid) {
        if(StringUtils.isEmpty(foreignBid)){
            return null;
        }
        ApmRoleIdentity apmRoleIdentity = getOne(Wrappers.<ApmRoleIdentity>lambdaQuery()
                .eq(ApmRoleIdentity::getForeignBid, foreignBid)
                .eq(ApmRoleIdentity::getDeleteFlag, DELETE_FLAG_NOT_DELETED));

        return ApmRoleIdentityConerter.INSTANCE.entity2VO(apmRoleIdentity);
    }

    @Override
    public boolean removeByRoleBids(List<String> roleBids) {
        if (CollectionUtils.isEmpty(roleBids)) {
            return false;
        }
        return remove(Wrappers.<ApmRoleIdentity>lambdaQuery().in(ApmRoleIdentity::getRoleBid, roleBids));
    }

    @Override
    public boolean removeByCondition(ApmRoleIdentity apmRoleIdentity, List<String> roleBids) {
        return remove(Wrappers.<ApmRoleIdentity>lambdaQuery()
                .in(CollectionUtils.isNotEmpty(roleBids), ApmRoleIdentity::getRoleBid, roleBids)
                .eq(Objects.nonNull(apmRoleIdentity.getId()), ApmRoleIdentity::getId, apmRoleIdentity.getId())
                .eq(StringUtils.isNotBlank(apmRoleIdentity.getForeignBid()), ApmRoleIdentity::getForeignBid, apmRoleIdentity.getForeignBid()));
    }

    @Override
    public boolean physicsRemove(ApmRoleIdentityDto apmRoleIdentityDto) {
        if (Objects.isNull(apmRoleIdentityDto)) {
            return false;
        }
        return apmRoleIdentityMapper.physicsRemove(apmRoleIdentityDto);
    }

    @Override
    public List<ApmRoleIdentity> listEmpByBizBidAndCodes(String sphereType, String bizBid, List<String> codes) {
        return this.getBaseMapper().listEmpByBizBidAndCodes(sphereType, bizBid, codes);
    }

    @Override
    public boolean saveRoleIdentity(ApmRoleIdentityAO apmRoleIdentityAo) {
        if (Objects.isNull(apmRoleIdentityAo)) {
            return false;
        }
        ApmRoleIdentity apmRoleIdentity = ApmRoleIdentityConerter.INSTANCE.ao2Entity(apmRoleIdentityAo);
        apmRoleIdentity.setCreatedBy(SsoHelper.getJobNumber());
        apmRoleIdentity.setCreatedTime(new Date());
        apmRoleIdentity.setDeleteFlag(DELETE_FLAG_NOT_DELETED);
        apmRoleIdentity.setEnableFlag(ENABLE_FLAG_ENABLE);
        return save(apmRoleIdentity);
    }

    @Override
    public boolean updateRoleIdentity(ApmRoleIdentityAO apmRoleIdentityAo) {
        if (Objects.isNull(apmRoleIdentityAo)) {
            return false;
        }
        ApmRoleIdentity apmRoleIdentity = ApmRoleIdentityConerter.INSTANCE.ao2Entity(apmRoleIdentityAo);
        return updateById(apmRoleIdentity);
    }
}




