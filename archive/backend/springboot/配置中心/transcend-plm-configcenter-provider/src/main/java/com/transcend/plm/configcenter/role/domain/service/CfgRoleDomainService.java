package com.transcend.plm.configcenter.role.domain.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.constant.RoleConst;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.role.infrastructure.repository.CfgRoleRepository;
import com.transcend.plm.configcenter.role.infrastructure.repository.CfgRoleUserService;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRolePo;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRoleUserPo;
import com.transcend.plm.configcenter.role.pojo.CfgRoleConverter;
import com.transcend.plm.configcenter.role.pojo.CfgRoleUserConverter;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
import com.transcend.plm.configcenter.role.pojo.vo.CfgRoleAndTypeVo;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgRoleDomainService implements IExcelStrategy {
    @Resource
    private CfgRoleRepository repository;

    @Resource
    private CfgRoleUserService cfgRoleUserService;

    public CfgRoleVo save(CfgRoleDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        return repository.save(cfgAttributeDto);
    }

    public CfgRoleVo update(CfgRoleDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        Assert.hasText(cfgAttributeDto.getBid(), "attribute bid is blank");
        return repository.update(cfgAttributeDto);
    }

    public CfgRoleVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgRoleVo> page(BaseRequest<CfgRoleQo> pageQo) {
        return repository.page(pageQo);
    }


    /**
     * 树查询
     *
     * @return
     */
    public List<CfgRoleVo> tree() {
        // 查询所有标准角色
        List<CfgRoleVo> cfgRoleVos = repository.listAll();
        // 组装树 时间复杂度需要2N，不能是 N*N
        // 根节点集合
        return convert2tree(cfgRoleVos);
    }

    public List<CfgRoleVo> listGlobalRole() {
        return repository.listGlobalRole();
    }


    /**
     * 根据角色code列表查询角色信息列表
     *
     * @param codeList 角色code列表
     * @return List<CfgRoleVo>
     */
    public List<CfgRoleVo> queryByCodes(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Lists.newArrayList();
        }
        return repository.queryByCodes(codeList);
    }

    /**
     * 组装树 时间复杂度需要2N，不能是 N*N
     *
     * @param cfgRoleVos 角色信息
     * @return
     */
    @NotNull
    private List<CfgRoleVo> convert2tree(List<? extends CfgRoleVo> cfgRoleVos) {
        List<CfgRoleVo> rootCfgRoleVos = Lists.newArrayList();
        // 父bid为key,子列表为value
        Map<String, List<CfgRoleVo>> parentKeyChildrenMap = Maps.newHashMap();
        cfgRoleVos.forEach(cfgRoleVo -> {
            // 父bid
            String parentBid = cfgRoleVo.getParentBid();
            if (CommonConst.ROLE_TREE_DEFAULT_ROOT_BID.equals(parentBid)) {
                // a.收集根节点
                rootCfgRoleVos.add(cfgRoleVo);
            } else {
                // b.收集父bid为key,子列表为value
                List<CfgRoleVo> children = parentKeyChildrenMap.get(parentBid);
                if (CollectionUtils.isEmpty(children)) {
                    children = Lists.newArrayList();
                    parentKeyChildrenMap.put(parentBid, children);
                }
                children.add(cfgRoleVo);
            }
        });
        recursiveSetChildren(rootCfgRoleVos, parentKeyChildrenMap);
        return rootCfgRoleVos.stream().sorted(Comparator.comparing(CfgRoleVo::getSort)).collect(Collectors.toList());
    }

    /**
     * 递归设置子
     */
    private void recursiveSetChildren(List<CfgRoleVo> roots,
                                      Map<String, List<CfgRoleVo>> parentCodeAndChildrenMap) {
        if (CollectionUtils.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            String parentCode = root.getBid();
            List<CfgRoleVo> children = parentCodeAndChildrenMap.get(parentCode);
            if (CollectionUtil.isNotEmpty(children)) {
                children.sort(Comparator.comparing(CfgRoleVo::getSort));
            }
            root.setChildren(children);
            this.recursiveSetChildren(children, parentCodeAndChildrenMap);
        });
    }

    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        CfgRoleVo vo = getByBid(bid);
        vo.setEnableFlag(enableFlag);
        CfgRoleDto dto = CfgRoleConverter.INSTANCE.vo2dto(vo);
        update(dto);
        return true;
    }

    public Boolean logicalDelete(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    /**
     * 查询业务和系统角色的组合
     *
     * @return List<CfgRoleAndTypeVo>
     */
    public List<CfgRoleAndTypeVo> treeAndSystem() {
        List<CfgRoleAndTypeVo> resultTree = Lists.newArrayList();
        // 1.构建业务角色/系统角色
        CfgRoleAndTypeVo sysTree = initDefaultRole(RoleConst.SYS_NAME);
        CfgRoleAndTypeVo bizTree = initDefaultRole(RoleConst.BIZ_NAME);
        resultTree.add(sysTree);
        resultTree.add(bizTree);
        // 2.填充系统和业务角色
        // 2.1 业务角色
        // 查询所有标准角色
        List<CfgRoleVo> cfgRoleVos = repository.listAll();
        // 遍历业务角色，并设置
        List<CfgRoleAndTypeVo> cfgRoleAndTypeVos = cfgRoleVos.stream()
                .filter(cfgRoleVo -> CommonConst.ENABLE_FLAG_ENABLE.equals(cfgRoleVo.getEnableFlag()))
                .map(cfgRoleVo -> {
                    CfgRoleAndTypeVo cfgRoleAndTypeVo = CfgRoleConverter.INSTANCE.vo2systemVo(cfgRoleVo);
                    cfgRoleAndTypeVo.setRoleType(RoleConst.BIZ_TYPE);
                    return cfgRoleAndTypeVo;
                }).collect(Collectors.toList());
        List<CfgRoleVo> bizRoleTree = convert2tree(cfgRoleAndTypeVos);
        bizTree.setChildren(bizRoleTree);
        // 2.2 系统角色 TODO 待处理
        return resultTree;
    }

    @NotNull
    private CfgRoleAndTypeVo initDefaultRole(String bizName) {
        CfgRoleAndTypeVo sysTree = CfgRoleAndTypeVo.of();
        sysTree.setName(bizName);
        sysTree.setDisabled(Boolean.TRUE);
        return sysTree;
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        List<CfgRoleVo> cfgRoleVos = repository.listAll();
        List<String> header = Lists.newArrayList("角色名称", "角色编码", "状态", "说明");
        List<Object> data = Lists.newArrayList();
        cfgRoleVos.forEach(cfgRoleVo -> {
            List<Object> row = Lists.newArrayList();
            row.add(cfgRoleVo.getName());
            row.add(cfgRoleVo.getCode());
            row.add(CommonConst.ENABLE_FLAG_ENABLE.equals(cfgRoleVo.getEnableFlag()) ? "启用" : "禁用");
            row.add(cfgRoleVo.getDescription());
            data.add(row);
        });
        List<Object> result = Lists.newArrayList();
        result.add(header);
        result.add(data);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer, Object>> dataList, ImportDto importDto) {
        if (CollectionUtils.isNotEmpty(dataList)) {
            List<CfgRoleVo> cfgRoleVos = repository.listAll();
            Map<String, String> codeBidMap = cfgRoleVos.stream().collect(Collectors.toMap(CfgRoleVo::getCode, CfgRoleVo::getBid));
            List<CfgRolePo> cfgRolePos = new ArrayList<>();
            String jobNumber = SsoHelper.getJobNumber();
            Map<String, String> parCodeMap = new HashMap<>();
            for (Map<Integer, Object> map : dataList) {
                if (map.get(0) == null || map.get(1) == null) {
                    throw new BusinessException("角色名称或编码不能为空");
                }
                if (codeBidMap.get(map.get(1) + "") != null) {
                    throw new BusinessException("编码" + map.get(1) + "重复");
                }
                CfgRolePo cfgRolePo = new CfgRolePo();
                cfgRolePo.setBid(SnowflakeIdWorker.nextIdStr());
                cfgRolePo.setCreatedTime(LocalDateTime.now());
                cfgRolePo.setUpdatedTime(LocalDateTime.now());
                cfgRolePo.setUpdatedBy(jobNumber);
                cfgRolePo.setCreatedBy(jobNumber);
                cfgRolePo.setName(map.get(0) + "");
                cfgRolePo.setCode(map.get(1) + "");
                if (map.get(2) == null) {
                    cfgRolePo.setParentBid("0");
                } else {
                    //设置父bid
                    parCodeMap.put(cfgRolePo.getCode(), map.get(2) + "");
                }
                if (map.get(3) != null) {
                    cfgRolePo.setDescription(map.get(3) + "");
                }
                codeBidMap.put(cfgRolePo.getCode(), cfgRolePo.getBid());
                cfgRolePos.add(cfgRolePo);
            }
            for (CfgRolePo cfgRolePo : cfgRolePos) {
                if (StringUtils.isEmpty(cfgRolePo.getParentBid())) {
                    cfgRolePo.setParentBid(codeBidMap.get(parCodeMap.get(cfgRolePo.getCode())));
                }
            }
            return repository.saveBatch(cfgRolePos);
        }
        return true;
    }

    public Boolean addUser(CfgRoleUserPo cfgRoleUserPo) {
        cfgRoleUserPo.setBid(SnowflakeIdWorker.nextIdStr());
        return cfgRoleUserService.save(cfgRoleUserPo);
    }

    public Boolean removeUser(String roleBid, String jobNumber) {
        if (StringUtils.isEmpty(roleBid) || StringUtils.isEmpty(jobNumber)) {
            throw new BusinessException("参数不能为空");
        }
        return cfgRoleUserService.removeByRoleBidAndJobNumber(roleBid, jobNumber);
    }

    @CacheEvict(value = CacheNameConstant.API_ROLE_USER_LIST, key = "#roleCode")
    public Boolean addUsers(String roleCode, List<CfgRoleUserPo> roleUserPos) {
        if (CollectionUtils.isEmpty(roleUserPos)) {
            throw new BusinessException("参数不能为空");
        }
        //用Stream获取jobNumber集合
        List<String> jobNumbers = roleUserPos.stream().map(CfgRoleUserPo::getJobNumber).distinct().collect(Collectors.toList());
        //查看数据库中是否已经存在
        List<CfgRoleUserPo> existRoleUserPos = cfgRoleUserService.listByRoleBidAndJobNumbers(roleUserPos.get(0).getRoleBid(), jobNumbers);
        if (CollectionUtils.isNotEmpty(existRoleUserPos)) {
            throw new BusinessException("用户已经存在：" + existRoleUserPos.stream().map(CfgRoleUserPo::getJobNumber).collect(Collectors.joining(",")));
        }
        //批量设置bid
        roleUserPos.forEach(cfgRoleUserPo -> cfgRoleUserPo.setBid(SnowflakeIdWorker.nextIdStr()));
        return cfgRoleUserService.saveBatch(roleUserPos);
    }

    public Boolean addUsers(List<CfgRoleUserPo> roleUserPos) {
        if (CollectionUtils.isEmpty(roleUserPos)) {
            throw new BusinessException("参数不能为空");
        }
        // 按照角色分组添加 (一个是性能，一个是缓存需要)
        Map<String, List<CfgRoleUserPo>> groupRoleUsers = roleUserPos.stream()
                .collect(Collectors.groupingBy(CfgRoleUserPo::getRoleCode));
        groupRoleUsers.forEach(this::addUsers);
        return true;
    }

    public List<CfgRoleUserVo> getUsersByRoleCode(String roleCode) {
        if (StringUtils.isEmpty(roleCode)) {
            throw new BusinessException("参数不能为空");
        }
        return CfgRoleUserConverter.INSTANCE.po2vo(cfgRoleUserService.listByRoleCode(roleCode));
    }

    public List<String> getRoleCodesByJobNumber(String jobNumber) {
        if (StringUtils.isEmpty(jobNumber)) {
            throw new BusinessException("参数不能为空");
        }
        return cfgRoleUserService.listRoleCodesByJobNumber(jobNumber);
    }

    @CacheEvict(value = CacheNameConstant.API_ROLE_USER_LIST, key = "#roleCode")
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAndAddUsers(List<CfgRoleUserPo> roleUserPos, String roleCode) {
        //roleCode不能为空
        if (StringUtils.isBlank(roleCode)) {
            throw new BusinessException("参数不能为空");
        }
        //删除原有用户
        cfgRoleUserService.removeByRoleCode(roleCode);
        //添加新用户
        if (CollectionUtils.isEmpty(roleUserPos)) {
            return Boolean.TRUE;
        }
        return addUsers(roleCode, roleUserPos);
    }

    public Set<String> listUserSetByRoleCode(String code) {
        List<CfgRoleUserVo> usersByRoleCode = getUsersByRoleCode(code);
        if (CollectionUtils.isEmpty(usersByRoleCode)) {
            return Collections.emptySet();
        }
        return usersByRoleCode.stream().map(CfgRoleUserVo::getJobNumber).collect(Collectors.toSet());
    }



}
