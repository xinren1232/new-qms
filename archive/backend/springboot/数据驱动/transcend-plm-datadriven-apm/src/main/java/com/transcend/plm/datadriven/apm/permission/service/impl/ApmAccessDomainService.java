package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.mapstruct.ApmAccessConverter;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmAccessVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigUserVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmAccessService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmAccessDomainService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmAccessDomainService implements IApmAccessDomainService {
    @Resource
    private ApmAccessService apmAccessService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmSphereService apmSphereService;

    @Override
    public ApmAccessVO save(ApmAccessAO apmAccessAO) {
        ApmAccess apmAccess = ApmAccessConverter.INSTANCE.ao2Entity(apmAccessAO);
        apmAccess.setBid(SnowflakeIdWorker.nextIdStr());
        apmAccess.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
        apmAccess.setCreatedBy(SsoHelper.getJobNumber());
        apmAccess.setCreatedTime(new Date());
        apmAccessService.save(apmAccess);
        return ApmAccessConverter.INSTANCE.entity2VO(apmAccess);
    }

    @Override
    public ApmAccessVO update(ApmAccessAO apmAccessAO) {
        ApmAccess apmAccess = apmAccessService.getOne(Wrappers.<ApmAccess>lambdaQuery().eq(ApmAccess::getBid, apmAccessAO.getBid()));
        if(StringUtil.isNotBlank(apmAccessAO.getName())){
            apmAccess.setName(apmAccessAO.getName());
        }
        if(StringUtil.isNotBlank(apmAccessAO.getResource())){
            apmAccess.setResource(apmAccessAO.getResource());
        }
        if(StringUtil.isNotBlank(apmAccessAO.getType())){
            apmAccess.setType(apmAccessAO.getType());
        }
        apmAccessService.updateById(apmAccess);
        return ApmAccessConverter.INSTANCE.entity2VO(apmAccess);
    }

    @Override
    public boolean logicDelete(String bid) {
        ApmAccess apmAccess = apmAccessService.getOne(Wrappers.<ApmAccess>lambdaQuery().eq(ApmAccess::getBid, bid));
        apmAccess.setDeleteFlag(1);
        return apmAccessService.updateById(apmAccess);
    }

    @Override
    public List<ApmAccessVO> list() {
        List<ApmAccess> apmAccessList = apmAccessService.list(Wrappers.<ApmAccess>lambdaQuery().eq(ApmAccess::getDeleteFlag, 0));
        return ApmAccessConverter.INSTANCE.entityList2VOList(apmAccessList);
    }

    @Override
    public List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String sphereBid) {
        return apmAccessService.queryAccessWithRoleBySphereBid(sphereBid);
    }

    @Override
    public List<ApmActionConfigUserVo> currentUserAccess(ApmSphere apmSphere) {
        // 获取当前登录的用户的角色
        String jobNum = SsoHelper.getJobNumber();
        // 通过BID 获取所有上级的BID
        List<String> shpereBidList = apmSphereService.getSphereBidListByBid(apmSphere.getBid());
        List<ApmRole> roleList = apmRoleService.getRoleListByJobNumAndSphereBidList(jobNum, shpereBidList);
        // 根据角色查询出对应的权限
        Set<String> roleBidSet = roleList.stream().map(ApmRole::getBid).collect(Collectors.toSet());
        List<ApmAccess> apmAccessList = apmAccessService.listByRoles(roleBidSet, apmSphere.getBid());
        return apmAccessList.stream().filter(e->e.getCode() != null).map(obj ->
                new ApmActionConfigUserVo()
                        .setDescription(obj.getDescription())
                        .setVisibleConfig(obj.getVisibleConfig())
                        .setOperationConfig(obj.getOperationConfig())
                        .setBid(obj.getBid())
                        .setAction(obj.getCode())
                        .setActionName(obj.getName())
                        .setResource(obj.getResource())
                        .setType(obj.getType())
                        .setSort(obj.getSort())
                        .setIcon(obj.getIcon())
                        .setMethodConfig(obj.getMethodConfig())
                        .setShowLocation(obj.getShowLocation())
        ).collect(Collectors.collectingAndThen(
                Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(ApmActionConfigUserVo::getAction))
                ), ArrayList::new)
        );

    }
}
