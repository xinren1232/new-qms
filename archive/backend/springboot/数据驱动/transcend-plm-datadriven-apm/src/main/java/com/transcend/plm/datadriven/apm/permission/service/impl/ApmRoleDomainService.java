package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.ApmIdentityTypeEnum;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleConverter;
import com.transcend.plm.datadriven.apm.permission.configcenter.SysRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.*;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.apm.springframework.config.GlobalRoleProperties;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.EsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色业务领域服务实现类
 * @createTime 2023-09-20 15:36:00
 */
@Service
public class ApmRoleDomainService implements IApmRoleDomainService {
    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmRoleIdentityService roleIdentityService;

    @Resource
    private IPlatformUserWrapper platformUserWrapper;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private GlobalRoleProperties globalRoleProperties;

    @Resource
    private SysRoleDomainService sysRoleDomainService;

    @Resource
    private OperationLogEsService operationLogEsService;

    @Override
    public ApmRoleVO update(ApmRoleAO apmRoleAO) {
        ApmRole apmRole = ApmRoleConverter.INSTANCE.ao2Entity(apmRoleAO);
        //检查角色编码是否重复
        if (isRoleExist(apmRoleAO)) {
            throw new PlmBizException("角色编码已存在");
        }
        //根据bid查询角色
        ApmRole existRole = apmRoleService.getByBid(apmRole.getBid());
        //判断pbid是否变更
        if (!existRole.getPbid().equals(apmRole.getPbid())) {
            updateChildrenPath(apmRole);
        }
        apmRoleService.updateByBid(apmRole);
        return null;
    }

    private void updateChildrenPath(ApmRole apmRole) {
        //获取父对象的path
        String path = getParentPath(apmRole.getPbid()) + RoleConstant.SEMICOLON + apmRole.getBid();
        //获取子对象列表
        List<ApmRole> children = apmRoleService.listChildrenByBid(apmRole.getBid());
        //更新子对象的path
        children.forEach(child -> {
            String childPath = child.getPath();
            String tempPath = childPath.substring(path.length());
            child.setPath(path + tempPath);
        });
        apmRole.setPath(path );
        apmRoleService.updateBatchById(children);
    }

    @Override
    public ApmRoleVO getByBid(String bid) {
        ApmRole apmRole = apmRoleService.getByBid(bid);
        return ApmRoleConverter.INSTANCE.entity2VO(apmRole);
    }

    @Override
    public Boolean deleteByBid(String bid) {
        //校验该角色是否为空间管理员，如果是，不允许删除
        ApmRole apmRole = apmRoleService.getByBid(bid);
        if(RoleConstant.SPACE_ADMIN_EN.equals(apmRole.getCode())){
            throw new TranscendBizException("空间管理员角色不允许删除");
        }
        String logMsg = String.format("删除 [%s]%s 角色", bid, apmRole.getName());
        operationLogEsService.simpleOperationLog(bid, logMsg, EsUtil.EsType.ROLE_IDENTITY);
        return apmRoleService.removeByBid(bid);
    }

    @Override
    public List<ApmRoleVO> list(ApmRoleQO apmRoleQO) {
        ApmRole apmRole = ApmRoleConverter.INSTANCE.qo2Entity(apmRoleQO);
        //查询sphereBid
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(apmRoleQO.getBizBid(), apmRoleQO.getBizType());
        if(apmSphere != null){
            apmRole.setSphereBid(apmSphere.getBid());
        }
        List<ApmRole> apmRoles = apmRoleService.listByCondition(apmRole);
        return ApmRoleConverter.INSTANCE.entityList2VOList(apmRoles);
    }

    @Override
    public ApmIdentity getIdentityByRole(String roleBid) {
        //根据roleBid,查看角色和identity的关系
        List<ApmRole> apmRoles = apmRoleService.listChildrenByBid(roleBid);
        if(CollectionUtils.isEmpty(apmRoles)){
            return null;
        }
        //用stream获取角色bid列表
        List<String> roleBids = apmRoles.stream().map(ApmRole::getBid).collect(Collectors.toList());
        List<ApmRoleIdentity> apmRoleIdentities = roleIdentityService.listByRoleBids(roleBids);
        //根据identity类型组装identity
        ListMultimap<String,ApmIdentity> apmIdentityMultimap = ArrayListMultimap.create();
        apmRoleIdentities.forEach(apmRoleIdentity -> {
            ApmIdentity apmIdentity = convertToIdentity(apmRoleIdentity);
            apmIdentityMultimap.put(apmRoleIdentity.getRoleBid(),apmIdentity);
        });
        //根据角色bid获取identity列表
        ListMultimap<String, ApmRoleBO> apmRoleIdentityMultimap = ArrayListMultimap.create();
        ApmRoleBO rootRoleBO = new ApmRoleBO();
        for (ApmRole apmRole : apmRoles) {
            //根据apmRole生成ApmRoleBO
            ApmRoleBO apmRoleBO = ApmRoleConverter.INSTANCE.entity2BO(apmRole);
            apmRoleBO.setRelatedApmIdentityList(apmIdentityMultimap.get(apmRole.getBid()));
            apmRoleIdentityMultimap.put(apmRole.getPbid(),apmRoleBO);
            if(apmRole.getBid().equals(roleBid)){
                rootRoleBO = apmRoleBO;
            }
        }
        //生成树形结构
        apmRoleIdentityMultimap.values().forEach(apmRoleBO -> {
            List<ApmRoleBO> children = apmRoleIdentityMultimap.get(apmRoleBO.getBid());
            if(CollectionUtils.isNotEmpty(children)){
                apmRoleBO.setChildren(children);
            }
        });
        return rootRoleBO;
    }

    @Override
    public List<ApmRoleAndIdentityVo> getRoleAndIdentityByRoleBids(List<String> roleBids) {
        return roleBids.stream().map(roleBid -> {
            ApmRoleAndIdentityVo apmRoleAndIdentityVo = new ApmRoleAndIdentityVo();
            //根据roleBid,查询下面的人员信息
            if(roleBid.equals(InnerRoleEnum.CREATER.getCode())){
                //内置创建人
                ApmRoleVO apmRoleVO = new ApmRoleVO();
                apmRoleVO.setBid(InnerRoleEnum.CREATER.getCode());
                apmRoleVO.setName(InnerRoleEnum.CREATER.getDesc());
                apmRoleVO.setCode(InnerRoleEnum.CREATER.getCode());
                apmRoleVO.setInnerRole(true);
                apmRoleAndIdentityVo.setApmUserList(new ArrayList<>());
                apmRoleAndIdentityVo.setApmRoleVO(apmRoleVO);
            }else{
                ApmIdentity identityByRole = getIdentityByRole(roleBid);
                if(identityByRole != null){
                    apmRoleAndIdentityVo.setApmUserList(identityByRole.getApmUserList());
                    // 根据roleBid,查询角色信息
                    ApmRole apmRole = apmRoleService.getByBid(roleBid);
                    apmRoleAndIdentityVo.setApmRoleVO(ApmRoleConverter.INSTANCE.entity2VO(apmRole));

                }
            }
            return apmRoleAndIdentityVo;

        }).collect(Collectors.toList());
    }

    @Override
    public List<ApmRole> listRoleByJobNumAndSphereBid(String jobNum, String sphereBid) {
        List<ApmRole> roleList = apmRoleService.getRoleListByJobNumAndSphereBid(jobNum, sphereBid);
        // 补充所有人员
        roleList.add(ApmRole.builder().code(RoleConstant.ALL).build());
        // 补充系统用户全局角色
        List<String> sysRoleCodeList = sysRoleDomainService.listSysRoleCode(jobNum);
        if (CollectionUtils.isNotEmpty(sysRoleCodeList)) {
            sysRoleCodeList.forEach(sysRole ->{
                roleList.add(ApmRole.builder().code(sysRole).build());
            }) ;
        }
        return roleList;
    }

    @Override
    public void addSphereAndDefaultRole() {
        // 找出所有空间、应用、实例的bid
        List<ApmSphere> apmSphereList = apmSphereService.list(Wrappers.<ApmSphere>lambdaQuery().in(ApmSphere::getType, Lists.newArrayList(TypeEnum.SPACE.getCode(), TypeEnum.OBJECT.getCode(), TypeEnum.INSTANCE.getCode())));
        if (CollectionUtils.isEmpty(apmSphereList)) {
            return;
        }
        List<String> sphereBidList = apmSphereList.stream().map(ApmSphere::getBid).collect(Collectors.toList());
        // 找出有全部成员角色的空间、应用、实例的sphereBid
        List<ApmRole> hasAllRoleCodeList = apmRoleService.list(Wrappers.<ApmRole>lambdaQuery().in(ApmRole::getSphereBid, sphereBidList).eq(ApmRole::getCode, RoleConstant.SPACE_ALL_EN));
        List<String> hasAllRoleCodeSphereBidList = hasAllRoleCodeList.stream().map(ApmRole::getSphereBid).collect(Collectors.toList());
        // 找出没有全部成员角色的空间、应用、实例的sphereBid
        List<String> notExistsAllRoleCodeList = sphereBidList.stream().filter(sphereBid -> !hasAllRoleCodeSphereBidList.contains(sphereBid)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(notExistsAllRoleCodeList)) {
            return;
        }
        List<ApmRole> apmRoleList = Lists.newArrayList();
        notExistsAllRoleCodeList.forEach(sphereBid -> {
            String roleBid = SnowflakeIdWorker.nextIdStr();
            ApmRole apmRole = new ApmRole()
                    .setCode(RoleConstant.SPACE_ALL_EN).setName(RoleConstant.SPACE_ALL_CH).setDescription(RoleConstant.SPACE_ALL_CH)
                    .setBid(roleBid).setPbid(RoleConstant.ROOT_BID)
                    .setPath(RoleConstant.ROOT_BID + RoleConstant.SEMICOLON + roleBid).setSphereBid(sphereBid)
                    .setCreatedBy(SsoHelper.getJobNumber()).setUpdatedBy(SsoHelper.getJobNumber()).setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
            apmRoleList.add(apmRole);
        });
        apmRoleService.saveBatch(apmRoleList);
    }

    @Override
    public Boolean isSpaceAdminOrGlobalAdmin(String spaceBid) {
        return isGlobalAdmin() || isSpaceAdmin(spaceBid);
    }

    @Override
    public Boolean isSpaceAdmin(String spaceBid) {
        if(Boolean.TRUE.equals(isGlobalAdmin())){
            return true;
        }
        //根据spaceBid查询sphereBid
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        //apmSphere为空，抛出异常
        if(apmSphere == null){
            throw new TranscendBizException("空间域不存在");
        }
        //查询当前登录人在该空间下有哪些角色
        List<ApmRole> apmRoles = apmRoleService.getRoleListByJobNumAndSphereBid(SsoHelper.getJobNumber(), apmSphere.getBid());
        if(CollectionUtils.isEmpty(apmRoles)){
            return false;
        }
        return apmRoles.stream().anyMatch(apmRole -> RoleConstant.SPACE_ADMIN_EN.equals(apmRole.getCode()));
    }

    @Override
    public Boolean isGlobalAdmin() {
        //获取全局管理员配置
        Set<String> administrators = globalRoleProperties.getAdministrators();
        if (CollectionUtils.isEmpty(administrators)) {
            return false;
        }
        return administrators.contains(SsoHelper.getJobNumber());
    }

    @Override
    public boolean remove(ApmRoleDto apmRoleDto) {
        if (Objects.isNull(apmRoleDto)) {
            return false;
        }
        ApmRole apmRole = ApmRoleConverter.INSTANCE.dto2Entity(apmRoleDto);
        return apmRoleService.removeByCondition(apmRole);
    }

    /**
     * 根据人缓存查询目标的角色集（包括空间，应用），另外需要把控角色人员配置时，需要删除改缓存  TODO LUOJIE
     *
     * @param jobNumber
     * @param spareBid
     * @return
     */
    @Override
    public List<String> listRolesByUser(String jobNumber, String spareBid) {
        return null;
    }

    private ApmIdentity convertToIdentity(ApmRoleIdentity apmRoleIdentity) {
        //根据apmRoleIdentity类型组装identity
        ApmIdentity apmIdentity = null;
        if (ApmIdentityTypeEnum.USER.getCode().equals(apmRoleIdentity.getType())) {
            ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(apmRoleIdentity.getIdentity());
            apmIdentity = new ApmUserBO(apmUser);
            //返回用户类型的identity
        } else if (ApmIdentityTypeEnum.DEPARTMENT.getCode().equals(apmRoleIdentity.getType())) {
            List<ApmUser> apmUserBOS = platformUserWrapper.listUserBOByDepartmentId(apmRoleIdentity.getIdentity(), true);
            ApmDepartmentBO apmDepartmentBO = new ApmDepartmentBO();
            apmDepartmentBO.setUserList(apmUserBOS);
            apmIdentity = apmDepartmentBO;
        }
        return apmIdentity;
    }

    @Override
    public ApmRoleVO add(ApmRoleAO apmRoleAO) {
        ApmRole apmRole = ApmRoleConverter.INSTANCE.ao2Entity(apmRoleAO);
        apmRole.setBid(SnowflakeIdWorker.nextIdStr());
        //检查角色编码是否重复
        if (isRoleExist(apmRoleAO)) {
            throw new PlmBizException("角色编码已存在");
        }
        String path = getParentPath(apmRole.getPbid());
        //根据type 查询sphereBid
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(apmRoleAO.getBizBid(), apmRoleAO.getBizType());
        if(apmSphere == null){
            throw new PlmBizException("空间域不存在");
        }
        apmRole.setSphereBid(apmSphere.getBid());
        apmRole.setPath(path + RoleConstant.SEMICOLON + apmRole.getBid());
        apmRole.setCreatedBy(SsoHelper.getJobNumber());
        apmRole.setUpdatedBy(apmRole.getCreatedBy());
        apmRoleService.save(apmRole);
        return ApmRoleConverter.INSTANCE.entity2VO(apmRole);
    }

    private String getParentPath(String pBid) {
        //获取父对象的path
        String path ;
        if (StringUtils.isNoneBlank(pBid) && !RoleConstant.ROOT_BID.equals(pBid)) {
            ApmRole parentRole = apmRoleService.getByBid(pBid);
            if (parentRole == null) {
                throw new PlmBizException("父角色不存在");
            }
            path = parentRole.getPath();
        } else {
            path = RoleConstant.ROOT_BID;
        }
        return path;
    }

    private boolean isRoleExist(ApmRoleAO apmRole) {
        if (StringUtils.isBlank(apmRole.getCode())) {
            return false;
        }
        // 兼容部分场景没传sphereBid
        String sphereBid = apmRole.getSphereBid();
        if (StringUtils.isBlank(sphereBid)) {
            sphereBid = Optional.ofNullable(apmSphereService.getByBizBidAndType(apmRole.getBizBid(), apmRole.getBizType()))
                    .orElse(new ApmSphere()).getBid();
        }
        ApmRole existedRole = apmRoleService.getByCodeAndSphere(apmRole.getCode(), sphereBid);
        if (existedRole == null) {
            return false;
        }
        return !existedRole.getBid().equals(apmRole.getBid());
    }
}
