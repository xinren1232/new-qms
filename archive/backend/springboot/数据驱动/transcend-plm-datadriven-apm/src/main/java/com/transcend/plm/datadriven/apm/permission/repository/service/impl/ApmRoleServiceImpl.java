package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.ApmIdentityTypeEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleConverter;
import com.transcend.plm.datadriven.apm.permission.configcenter.SysRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAddAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmRoleMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleDto;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.DeptConst;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @description 针对表【apm_role】的数据库操作Service实现
 * @createDate 2023-09-20 16:15:29
 */
@Service
@Slf4j
public class ApmRoleServiceImpl extends ServiceImpl<ApmRoleMapper, ApmRole> implements ApmRoleService {

    @Resource
    private ApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Resource
    private PlatformUserWrapper platformUserWrapper;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleMapper apmRoleMapper;

    @Resource
    private IApmRoleDomainService apmRoleDomainService;

    @Resource
    private SysRoleDomainService sysRoleDomainService;

    @Value("${transcend.default.space.administrator:18645974}")
    private String defaultSpaceAdministrator;


    @Override
    public boolean updateByBid(ApmRole apmRole) {
        ApmRole apmRole1 = getByBid(apmRole.getBid());
        if (StringUtil.isNotBlank(apmRole.getName())) {
            apmRole1.setName(apmRole.getName());
        }
        if (StringUtil.isNotBlank(apmRole.getCode())) {
            apmRole1.setCode(apmRole.getCode());
        }
        apmRole1.setDescription(apmRole.getDescription());
        return updateById(apmRole1);
    }

    @Override
    public Boolean removeByBid(String bid) {
        return remove(Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getBid, bid));
    }

    @Override
    public List<ApmRole> listByCondition(ApmRole apmRole) {
        //根据apmRole的值进行查询
        return list(Wrappers.<ApmRole>lambdaQuery()
                .eq(StringUtils.isNotBlank(apmRole.getBid()), ApmRole::getBid, apmRole.getBid())
                .like(StringUtils.isNotBlank(apmRole.getCode()), ApmRole::getCode, apmRole.getCode())
                .like(StringUtils.isNotBlank(apmRole.getName()), ApmRole::getName, apmRole.getName())
                .eq(StringUtils.isNotBlank(apmRole.getSphereBid()), ApmRole::getSphereBid, apmRole.getSphereBid())
                .eq(StringUtils.isNotBlank(apmRole.getCreatedBy()), ApmRole::getCreatedBy, apmRole.getCreatedBy())
                .eq(ApmRole::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
    }

    /**
     * 根据角色id列表查询角色信息
     *
     * @param bids 角色id列表
     * @return
     */
    public List<ApmRole> listByBids(List<String> bids) {
        //根据apmRole的值进行查询
        return list(Wrappers.<ApmRole>lambdaQuery()
                .in(ApmRole::getBid, bids)
                .eq(ApmRole::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
    }

    @Override
    public ApmRole getByBid(String bid) {
        return getOne(Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getBid, bid));
    }

    @Override
    public List<ApmRole> listChildrenByBid(String bid) {
        ApmRole apmRole = getByBid(bid);
        if (apmRole == null) {
            return Lists.newArrayList();
        }
        List<ApmRole> apmRoles = list(Wrappers.<ApmRole>lambdaQuery().likeRight(ApmRole::getPath, apmRole.getPath() + RoleConstant.SEMICOLON));
        apmRoles.add(apmRole);
        return apmRoles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDefaultRole(String spaceName, String sphereBid) {
        HashMap<String, ApmRole> name2RoleObjMap = Maps.newHashMap();
        name2RoleObjMap.put(RoleConstant.SPACE_ADMIN_CH, buildDefaultRole(RoleConstant.SPACE_ADMIN_EN, RoleConstant.SPACE_ADMIN_CH, sphereBid));
        name2RoleObjMap.put(RoleConstant.SPACE_MEMBER_CH, buildDefaultRole(RoleConstant.SPACE_MEMBER_EN, RoleConstant.SPACE_MEMBER_CH, sphereBid));
        name2RoleObjMap.put(RoleConstant.SPACE_ALL_EN, buildDefaultRole(RoleConstant.SPACE_ALL_EN, RoleConstant.SPACE_ALL_CH, sphereBid));
        // 保存默认角色
        saveBatch(name2RoleObjMap.values());
        // 为默认角色赋予权限

        // 保存默认角色之后，创建默认角色与成员的关系
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(SsoHelper.getJobNumber());
        String deptName = "";
        if (apmUser == null) {
            apmUser = platformUserWrapper.getUserBOByEmpNO(defaultSpaceAdministrator);
            log.error("当前用户不存在:{}", SsoHelper.getJobNumber());
        }
        if (CollectionUtils.isNotEmpty(apmUser.getDepts()) && StringUtils.isNotBlank(apmUser.getDepts().get(0).getName())) {
            deptName = apmUser.getDepts().get(0).getName();

        }
        HashMap<String, Object> employMap = Maps.newHashMap();
        employMap.put(RoleConstant.EMPLOYEE_NO, apmUser.getEmpNo());
        employMap.put(RoleConstant.EMPLOYEE_NAME, apmUser.getName());
        employMap.put(RoleConstant.DEPT_NAME, deptName);
        ApmRoleIdentityAddAO apmRoleIdentityAddAo = new ApmRoleIdentityAddAO()
                .setRoleBid(name2RoleObjMap.get(RoleConstant.SPACE_ADMIN_CH).getBid())
                .setDataList(Lists.newArrayList(employMap))
                .setType(ApmIdentityTypeEnum.USER.getCode());
        // saveOrUpdate方法封装了返回的提示信息，不是原来的boolean类型，所以这里使用getData()
        return apmRoleIdentityDomainService.saveOrUpdate(apmRoleIdentityAddAo).getData();
    }

    @Override
    public Map<String, String> copyRoles(Map<String, String> sphereBidMap, Set<String> notInCodes) {
        List<ApmRole> list = list(Wrappers.<ApmRole>lambdaQuery().notIn(ApmRole::getCode, notInCodes).in(ApmRole::getSphereBid, sphereBidMap.keySet()));
        Map<String, String> bidMap = new HashMap<>(16);
        for (ApmRole apmRole : list) {
            apmRole.setId(null);
            apmRole.setSphereBid(sphereBidMap.get(apmRole.getSphereBid()));
            String bid = SnowflakeIdWorker.nextIdStr();
            bidMap.put(apmRole.getBid(), bid);
            apmRole.setBid(bid);
            apmRole.setCreatedTime(new Date());
            apmRole.setUpdatedTime(new Date());
        }
        for (ApmRole apmRole : list) {
            apmRole.setPbid(bidMap.getOrDefault(apmRole.getPbid(), CommonConst.ROLE_TREE_DEFAULT_ROOT_BID));
            apmRole.setParentBid(bidMap.getOrDefault(apmRole.getParentBid(), CommonConst.ROLE_TREE_DEFAULT_ROOT_BID));
        }
        saveBatch(list);
        return bidMap;
    }

    @Override
    public List<ApmRoleVO> getRoleListBySphereBid(String sphereBid) {
        LambdaQueryWrapper<ApmRole> roleListQueryWrapper = Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getSphereBid, sphereBid);
        roleListQueryWrapper.last("order by FIELD(code, 'all') desc");
        List<ApmRole> apmRoles = this.baseMapper.selectList(roleListQueryWrapper);
        return ApmRoleConverter.INSTANCE.entityList2VOList(apmRoles);
    }

    @Override
    public List<String> getRoleListBySphereBidAndIdentityList(String sphereBid, List<String> identityList) {
        return this.baseMapper.getRoleListBySphereBidAndIdentityList(sphereBid, identityList);
    }

    @Override
    public List<ApmRole> getRoleListByJobNumAndSphereBid(String jobNum, String sphereBid) {
        if (StrUtil.isBlank(jobNum)) {
            return Lists.newArrayList();
        }
        // 查询当前登录人以及其部门的角色信息
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(jobNum);
        // 这里只能查到最底层的部门
        List<String> departmentList = apmUser.getDepts().stream().map(DepartmentDTO::getDeptNo).collect(Collectors.toList());
        // 需要根据最底层的部门向上反查所有的部门
        List<DepartmentDTO> parentDeptList = platformUserWrapper.getParentDepartmentByDepartmentId(apmUser.getDepartmentList().get(0));
        for (DepartmentDTO departmentDTO : parentDeptList) {
            departmentList.add(String.valueOf(departmentDTO.getDeptNo()));
        }
        departmentList.add(jobNum);
        return this.baseMapper.getRoleListByJobNumAndSphereBid(departmentList, sphereBid);
    }

    @Override
    public ThreeDeptVO queryThreeDeptInfo(String jobNum) {
        // 1. 构建返回结果
        ThreeDeptVO threeDeptVO = ThreeDeptVO.builder().build();

        // 2. 参数校验并提前返回
        if (StrUtil.isBlank(jobNum)) {
            return threeDeptVO;
        }

        // 3. 查询用户信息
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(jobNum);
        if (apmUser == null || CollectionUtil.isEmpty(apmUser.getDepts())) {
            return threeDeptVO;
        }

        // 4. 获取用户当前部门
        String currentDeptId = apmUser.getDepts().get(0).getDeptNo();
        DepartmentDTO currentDepartment = platformUserWrapper.getDepartmentByDepartmentId(currentDeptId);
        if (currentDepartment == null) {
            return threeDeptVO;
        }

        // 5. 处理当前部门
        convertThreeLeverDept(threeDeptVO, currentDepartment);

        // 6. 如果已经是一级部门，直接返回结果
        if (DeptConst.DEPT_LEVEL_3.equals(currentDepartment.getLevel())) {
            return threeDeptVO;
        }

        // 7. 查询并处理父级部门
        List<DepartmentDTO> parentDepts = platformUserWrapper.getParentDepartmentByDepartmentId(currentDeptId);
        if (CollectionUtil.isNotEmpty(parentDepts)) {
            parentDepts.forEach(dept -> convertThreeLeverDept(threeDeptVO, dept));
        }

        return threeDeptVO;
    }

    @Override
    public List<String> getAllChildCode(String roleCode) {
        return this.getBaseMapper().getAllChildCode(roleCode);
    }

    @Override
    public List<String> getAllChildBid(String bid) {
        return this.getBaseMapper().getAllChildBid(bid);
    }

    /**
     * 三级部门转换方法
     *
     * @param threeDeptVO   threeDeptVO
     * @param departmentDTO departmentDTO
     */
    private void convertThreeLeverDept(ThreeDeptVO threeDeptVO, DepartmentDTO departmentDTO) {
        Integer level = departmentDTO.getLevel();
        if (DeptConst.DEPT_LEVEL_3.equals(level)) {
            threeDeptVO.setFirstDeptId(departmentDTO.getDeptNo());
            threeDeptVO.setFirstDeptName(departmentDTO.getName());
        }
        if (DeptConst.DEPT_LEVEL_4.equals(level)) {
            threeDeptVO.setSecondDeptId(departmentDTO.getDeptNo());
            threeDeptVO.setSecondDeptName(departmentDTO.getName());
        }
        if (DeptConst.DEPT_LEVEL_5.equals(level)) {
            threeDeptVO.setThirdDeptId(departmentDTO.getDeptNo());
            threeDeptVO.setThirdDeptName(departmentDTO.getName());
        }
    }

    @Override
    public List<ApmRole> getRoleListByJobNumAndSphereBidList(String jobNum, List<String> sphereBids) {
        if (StrUtil.isBlank(jobNum)) {
            return Lists.newArrayList();
        }
        // 查询当前登录人以及其部门的角色信息
        ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(jobNum);
        // 需要根据最底层的部门向上反查所有的部门
        List<DepartmentDTO> parentDeptList = platformUserWrapper.getParentDepartmentByDepartmentId(apmUser.getDepartmentList().get(0));
        // 这里只能查到最底层的部门
        List<String> departmentList = apmUser.getDepts().stream().map(DepartmentDTO::getDeptNo).collect(Collectors.toList());
        for (DepartmentDTO departmentDTO : parentDeptList) {
            departmentList.add(String.valueOf(departmentDTO.getDeptNo()));
        }
        departmentList.add(jobNum);
        return this.baseMapper.getRoleListByJobNumAndSphereBidList(departmentList, sphereBids);
    }

    /**
     * @param roleBids
     * @param sphereBid
     * @return
     */
    @Override
    public List<ApmRoleVO> listByRoleBidsAndSpaceAppBid(List<String> roleBids, String sphereBid) {
        List<ApmRole> list = list(Wrappers.<ApmRole>lambdaQuery()
                .in(ApmRole::getBid, roleBids).eq(ApmRole::getSphereBid, sphereBid));
        return ApmRoleConverter.INSTANCE.entitys2vos(list);
    }

    @Override
    public List<ApmRoleVO> listByRoleBidsByCodes(List<String> codes, String sphereBid) {
        List<ApmRole> list = list(Wrappers.<ApmRole>lambdaQuery()
                .in(ApmRole::getCode, codes).eq(ApmRole::getSphereBid, sphereBid));
        return ApmRoleConverter.INSTANCE.entitys2vos(list);
    }

    /**
     * 方法描述
     *
     * @param code      code
     * @param sphereBid sphereBid
     * @return 返回值
     */
    @Override
    public ApmRoleVO getByRoleBidsByCode(String code, String sphereBid) {
        ApmRole apmRole = getOne(Wrappers.<ApmRole>lambdaQuery()
                .eq(ApmRole::getCode, code).eq(ApmRole::getSphereBid, sphereBid));
        return ApmRoleConverter.INSTANCE.entity2VO(apmRole);
    }

    @Override
    public List<ApmRoleVO> listByRoleBids(List<String> roleBids) {
        if (CollectionUtils.isEmpty(roleBids)) {
            return null;
        }
        List<ApmRole> list = list(Wrappers.<ApmRole>lambdaQuery()
                .in(ApmRole::getBid, roleBids));
        return ApmRoleConverter.INSTANCE.entitys2vos(list);
    }

    @Override
    public ApmRole getByCodeAndApp(String code, String spaceAppBid) {
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (apmSphere == null) {
            throw new PlmBizException("空间应用对应域不存在");
        }
        return getByCodeAndSphere(code, apmSphere.getBid());
    }

    @Override
    @Cacheable(value = CacheNameConstant.ROLE, key = "#foreignBid")
    public ApmRoleVO getApmRoleVOByForeignBid(String foreignBid) {
        if (StringUtils.isEmpty(foreignBid)) {
            return null;
        }
        ApmRole apmRole = getOne(Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getForeignBid, foreignBid)
                .eq(ApmRole::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));

        return ApmRoleConverter.INSTANCE.entity2VO(apmRole);
    }

    @Override
    public boolean createRole(ApmRoleDto apmRoleDto) {
        if (Objects.isNull(apmRoleDto)) {
            return false;
        }
        ApmRole apmRole = ApmRoleConverter.INSTANCE.dto2Entity(apmRoleDto);
        return save(apmRole);
    }

    @Override
    public boolean removeByCondition(ApmRole apmRole) {
        return remove(Wrappers.<ApmRole>lambdaQuery()
                .eq(StringUtils.isNotBlank(apmRole.getSphereBid()), ApmRole::getSphereBid, apmRole.getSphereBid())
                .eq(StringUtils.isNotBlank(apmRole.getBid()), ApmRole::getBid, apmRole.getBid()));
    }

    @Override
    public boolean physicsRemove(ApmRoleDto apmRoleDto) {
        if (Objects.isNull(apmRoleDto)) {
            return false;
        }
        return apmRoleMapper.physicsRemove(apmRoleDto);
    }

    public ApmRole buildDefaultRole(String code, String nameAndDesc, String sphereBid) {
        String roleBid = SnowflakeIdWorker.nextIdStr();
        return new ApmRole()
                .setCode(code)
                .setName(nameAndDesc)
                .setDescription(nameAndDesc)
                .setBid(roleBid)
                .setPbid(RoleConstant.ROOT_BID)
                .setPath(RoleConstant.ROOT_BID + RoleConstant.SEMICOLON + roleBid)
                .setSphereBid(sphereBid)
                .setType(TypeEnum.SPACE.getCode())
                .setCreatedBy(SsoHelper.getJobNumber())
                .setUpdatedBy(SsoHelper.getJobNumber())
                .setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE);
    }

    @Override
    public ApmRole getByCodeAndSphere(String code, String sphereBid) {
        return getOne(Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getCode, code).eq(ApmRole::getSphereBid, sphereBid));
    }

    @Override
    public List<String> getRoleCodeList(String spaceBid, String userNO) {
        List<String> result = org.apache.commons.compress.utils.Lists.newArrayList();
        // 补充系统用户全局角色
        List<String> sysRoleCodeList = sysRoleDomainService.listSysRoleCode(userNO);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(sysRoleCodeList)) {
            result.addAll(sysRoleCodeList);
        }
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        if (apmSphere != null) {
            List<ApmRole> apmRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(userNO, apmSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoles)) {
                // 如果在该空间下有任意非"ALL"角色，则补充空间角色
                if (apmRoles.stream().anyMatch(role -> apmSphere.getBid().equals(role.getSphereBid()) && !RoleConstant.ALL.equals(role.getCode()))) {
                    result.add(RoleConstant.SPACE_MEMBER_EN);
                }
                List<String> roleCodes = apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList());
                result.addAll(roleCodes);
                return result;
            }
        }
        if (!result.contains(RoleConstant.ALL)) {
            result.add(RoleConstant.ALL);
        }
        return result;
    }

    @Override
    public String getSpaceRoleBid(String spaceBid, String roleCode) {
        Assert.notNull(spaceBid, "spaceBid must not be null");
        Assert.notNull(roleCode, "roleCode must not be null");
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        Assert.notNull(apmSphere, "apmSphere must not be null");
        return this.getObj(this.lambdaQuery().select(ApmRole::getBid).getWrapper()
                        .eq(ApmRole::getCode, roleCode)
                        .eq(ApmRole::getSphereBid, apmSphere.getBid())
                        .eq(ApmRole::getDeleteFlag, 0),
                String::valueOf);
    }
}




