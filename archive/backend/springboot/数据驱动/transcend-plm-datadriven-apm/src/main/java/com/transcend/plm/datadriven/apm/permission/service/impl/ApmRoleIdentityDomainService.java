package com.transcend.plm.datadriven.apm.permission.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgRoleFeignClient;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmCopyRoleAndIdentityQo;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.mapstruct.ApmRoleIdentityConerter;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAddAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.entity.MemberInputStat;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.repository.service.MemberInputStatService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmRoleIdentityDto;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.RoleConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.EsUtil;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Sets;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.api.model.RelationEnum.SPACE_APP_BID;
import static com.transcend.plm.datadriven.api.model.RelationEnum.SPACE_BID;
import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.PROJECT_MODEL_CODE;

/**
 * @author unknown
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApmRoleIdentityDomainService implements IApmRoleIdentityDomainService {
    @Resource
    private ApmRoleIdentityService apmRoleIdentityService;
    @Resource
    private ApmRoleDomainService apmRoleDomainService;
    @Resource
    private ApmSphereService apmSphereService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ApmRoleService apmRoleService;
    @Resource
    private CfgRoleFeignClient cfgRoleFeignClient;

    @Resource
    private MemberInputStatService memberInputStatService;

    @Resource
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private OperationLogEsService operationLogEsService;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;


    @Override
    public TranscendApiResponse<Boolean> saveOrUpdate(ApmRoleIdentityAddAO apmRoleIdentityAddAO) {
        // 移除内置角色的新增
        String roleBid = apmRoleIdentityAddAO.getRoleBid();
        if (StringUtils.isNotEmpty(roleBid) && InnerRoleEnum.getCodes().contains(roleBid)) {
            throw new TranscendBizException("内置角色不允许增加成员");
        }
        //判断逻辑 如果apmRoleAccessAOS 为空 即删除功能
        if (CollectionUtils.isNotEmpty(apmRoleIdentityAddAO.getDataList())) {
            List<ApmRoleIdentityAO> apmRoleIdentityAOS = new ArrayList<>();
            //人员
            if (TypeEnum.EMPLOYEE.getCode().equals(apmRoleIdentityAddAO.getType())) {

                // 最大百分比值
                int maxPercentageValue = 100;
                // 员工号列表
                List<String> identityList = new ArrayList<>();

                for (Map<String, Object> map : apmRoleIdentityAddAO.getDataList()) {
                    ApmRoleIdentityAO apmRoleIdentityAO = new ApmRoleIdentityAO();

                    // 变量定义与接收
                    String employeeName = map.get("employeeName") + "";
                    Object employeeNoObj = map.get("employeeNo");
                    // 检查员工号是否为空
                    Assert.isTrue(null != employeeNoObj, employeeName + " 员工号为空，请检查");

                    String employeeNo = employeeNoObj + "";
                    String deptName = map.get("deptName") + "";

                    // 赋值
                    apmRoleIdentityAO.setType(TypeEnum.EMPLOYEE.getCode());
                    apmRoleIdentityAO.setIdentity(employeeNo);
                    apmRoleIdentityAO.setName(employeeName);
                    apmRoleIdentityAO.setDeptName(deptName);

                    // 判空与赋值
                    if (!ObjectUtils.isEmpty(map.get("inputPercentage"))) {

                        Integer inputPercentage = (Integer) map.get("inputPercentage");
                        // 检查百分比是否大于100%
                        if (inputPercentage > maxPercentageValue) {
                            return TranscendApiResponse.success(employeeName + " 投入百分比已超出百分百，请重新设置值", false);
                        }
                        apmRoleIdentityAO.setInputPercentage(inputPercentage);
                    }

                    identityList.add(employeeNo);
                    apmRoleIdentityAOS.add(apmRoleIdentityAO);
                }

                // 查询当前角色bid之外类型为employee的员工列表
                List<ApmRoleIdentity> identityData = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery()
                        .ne(ApmRoleIdentity::getRoleBid, roleBid)
                        .eq(ApmRoleIdentity::getType, apmRoleIdentityAddAO.getType())
                        .in(ApmRoleIdentity::getIdentity, identityList)
                        .eq(ApmRoleIdentity::getDeleteFlag, 0));

                // 查询每个员工在其它角色bid的百分比值
                if (CollectionUtil.isNotEmpty(identityData)) {
                    Map<String, IntSummaryStatistics> perEmployeeTotalPercentage = identityData.stream().collect(Collectors.groupingBy(ApmRoleIdentity::getIdentity, Collectors.summarizingInt(s -> null == s.getInputPercentage() ? 0 : s.getInputPercentage())));
                    for (ApmRoleIdentityAO vo : apmRoleIdentityAOS) {
                        IntSummaryStatistics summaryStatistics = perEmployeeTotalPercentage.get(vo.getIdentity());
                        // 判断总百分比值是否超出最大值
                        long sum = 0L;
                        if (null != summaryStatistics) {
                            sum = summaryStatistics.getSum();
                        }
                        Integer inputPercentage = vo.getInputPercentage();
                        inputPercentage = inputPercentage == null ? 0 : inputPercentage;
                        long totalValue = sum + inputPercentage;
                        long surplusValue = maxPercentageValue - sum;
                        if (totalValue > maxPercentageValue) {
                            return TranscendApiResponse.success(vo.getName() + " 总投入百分比已超出百分百，剩余最大可设置人员投入百分比值：" + surplusValue + "，请重新设置值", false);
                        }
                    }
                }
            }
            //部门
            if (TypeEnum.DEPARTMENT.getCode().equals(apmRoleIdentityAddAO.getType())) {
                for (Map<String, Object> map : apmRoleIdentityAddAO.getDataList()) {
                    ApmRoleIdentityAO apmRoleIdentityAO = new ApmRoleIdentityAO();
                    apmRoleIdentityAO.setType(TypeEnum.DEPARTMENT.getCode());
                    apmRoleIdentityAO.setIdentity(map.get("deptNo") + "");
                    apmRoleIdentityAO.setName(map.get("deptName") + "");
                    apmRoleIdentityAO.setDeptName(apmRoleIdentityAO.getName());
                    apmRoleIdentityAOS.add(apmRoleIdentityAO);
                }
            }
            apmRoleIdentityAddAO.setApmRoleIdentityAOS(apmRoleIdentityAOS);
        }
        String roleName = apmRoleService.getObj(apmRoleService.lambdaQuery().select(ApmRole::getName).getWrapper()
                .eq(ApmRole::getBid, roleBid).last("LIMIT 1"), String::valueOf);
        if (CollectionUtils.isEmpty(apmRoleIdentityAddAO.getApmRoleIdentityAOS())) {
            boolean remove = apmRoleIdentityService.remove(Wrappers.<ApmRoleIdentity>lambdaQuery().eq(ApmRoleIdentity::getRoleBid, roleBid)
                    .eq(ApmRoleIdentity::getType, apmRoleIdentityAddAO.getType()));
            if (remove) {
                String logMsg = String.format("删除 [%s]%s 全部的[%s]配置", roleBid, roleName, apmRoleIdentityAddAO.getType());
                operationLogEsService.simpleOperationLog(roleBid, logMsg, EsUtil.EsType.ROLE_IDENTITY);
            }
            return TranscendApiResponse.success(remove);
        } else {
            //先删除原来的数据 在保存最新的
            List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery()
                    .eq(ApmRoleIdentity::getRoleBid, roleBid)
                    .eq(ApmRoleIdentity::getType, apmRoleIdentityAddAO.getType())
                    .eq(ApmRoleIdentity::getDeleteFlag, 0));
            if (CollectionUtils.isNotEmpty(apmRoleIdentityList)) {
                List<Integer> ids = new ArrayList<>();
                for (ApmRoleIdentity apmRoleIdentity : apmRoleIdentityList) {
                    ids.add(apmRoleIdentity.getId());
                }
                apmRoleIdentityService.removeByIds(ids);
            }
            //新增数据
            for (ApmRoleIdentityAO apmRoleIdentityAO : apmRoleIdentityAddAO.getApmRoleIdentityAOS()) {
                apmRoleIdentityAO.setRoleBid(roleBid);
            }
            String jobNumber = SsoHelper.getJobNumber();
            List<ApmRoleIdentity> apmRoleIdentities = ApmRoleIdentityConerter.INSTANCE.aoList2EntityList(apmRoleIdentityAddAO.getApmRoleIdentityAOS());
            for (ApmRoleIdentity apmRoleIdentity : apmRoleIdentities) {
                apmRoleIdentity.setCreatedBy(jobNumber);
                apmRoleIdentity.setCreatedTime(new Date());
                apmRoleIdentity.setDeleteFlag(0);
                apmRoleIdentity.setEnableFlag(1);
            }
            boolean saveBatch = apmRoleIdentityService.saveBatch(apmRoleIdentities);
            if (saveBatch) {
                String updateBefore = apmRoleIdentityList.stream().map(this::getIdentityName)
                        .collect(Collectors.joining(","));
                String updateAfter = apmRoleIdentities.stream().map(this::getIdentityName)
                        .collect(Collectors.joining(","));
                String logMsg = String.format("修改 [%s]%s : %s => %s", roleBid, roleName, updateBefore, updateAfter);
                operationLogEsService.simpleOperationLog(roleBid, logMsg, EsUtil.EsType.ROLE_IDENTITY);
            }
            return TranscendApiResponse.success(saveBatch);
        }
    }

    /**
     * 获取身份名称
     *
     * @param identity 身份标识
     * @return 身份名称
     */
    private String getIdentityName(ApmRoleIdentity identity) {
        return String.format("%s(%s)",
                TypeEnum.DEPARTMENT.getCode().equals(identity.getType()) ? identity.getDeptName() : identity.getName(),
                identity.getIdentity());
    }


    @Override
    public boolean deleteById(Integer id) {
        //根据ID获取数据
        ApmRoleIdentity apmRoleIdentity = apmRoleIdentityService.getById(id);
        if (apmRoleIdentity == null) {
            throw new TranscendBizException("数据不存在");
        }
        //根据roleBid获取角色
        String roleBid = apmRoleIdentity.getRoleBid();
        ApmRoleVO apmRoleVO = Optional.ofNullable(apmRoleDomainService.getByBid(roleBid)).orElseThrow(() -> new TranscendBizException("角色不存在"));
        //如果是空间管理员，判断是否还有其他空间管理员
        if (RoleConstant.SPACE_ADMIN_EN.equals(apmRoleVO.getCode())) {
            List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentityService
                    .list(Wrappers.<ApmRoleIdentity>lambdaQuery()
                            .eq(ApmRoleIdentity::getRoleBid, roleBid)
                    );
            if (CollectionUtils.isNotEmpty(apmRoleIdentityList)) {
                boolean flag = false;
                for (ApmRoleIdentity apmRoleIdentity1 : apmRoleIdentityList) {
                    if (!apmRoleIdentity1.getId().equals(id)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    throw new TranscendBizException("空间管理员必须存在一个");
                }
            }
        }
        boolean remove = apmRoleIdentityService.removeById(id);
        if (remove) {
            String logMsg = String.format("删除 [%s]%s 中的 %s 配置",
                    roleBid, apmRoleVO.getName(), this.getIdentityName(apmRoleIdentity));
            operationLogEsService.simpleOperationLog(roleBid, logMsg, EsUtil.EsType.ROLE_IDENTITY);
        }
        return remove;
    }

    @Override
    public List<ApmRoleIdentityVO> listByRoleBid(String roleBid, String type, String name) {
        // 全部成员特殊处理
        List<ApmRoleIdentity> apmRoleIdentityList;
        ApmRoleVO apmRoleVO = Optional.ofNullable(apmRoleDomainService.getByBid(roleBid)).orElseThrow(() -> new BusinessException("角色不存在"));
        // 全局系统角色从配置中心捞人
        if (RoleConst.SYS_GLOBAL_ROLE_TYPE.contains(apmRoleVO.getType())) {
            return listSysUser(apmRoleVO.getCode());
        }
        if (RoleConstant.SPACE_ALL_EN.equals(apmRoleVO.getCode())) {
            apmRoleIdentityList = apmRoleIdentityService.listAllBySphereBid(apmRoleVO.getSphereBid(), type, name);
        } else {
            apmRoleIdentityList = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery().eq(ApmRoleIdentity::getRoleBid, roleBid)
                    .eq(ApmRoleIdentity::getType, type)
                    .like(StringUtils.isNotBlank(name), ApmRoleIdentity::getName, name)
                    .eq(ApmRoleIdentity::getDeleteFlag, 0)
                    .orderByAsc(ApmRoleIdentity::getCreatedTime)
            );
        }
        List<ApmRoleIdentityVO> resList = ApmRoleIdentityConerter.INSTANCE.entityList2VOList(apmRoleIdentityList);
        for (ApmRoleIdentityVO apmRoleIdentityVO : resList) {
            if (apmRoleIdentityVO.getType().equals(TypeEnum.EMPLOYEE.getCode())) {
                apmRoleIdentityVO.setEmployeeName(apmRoleIdentityVO.getName());
                apmRoleIdentityVO.setEmployeeNo(apmRoleIdentityVO.getIdentity());
            } else {
                apmRoleIdentityVO.setDeptNo(apmRoleIdentityVO.getIdentity());
                apmRoleIdentityVO.setDeptName(apmRoleIdentityVO.getName());
            }
        }

        return resList;
    }

    @Override
    public List<ApmRoleIdentityVO> spaceIdentityList(String spaceBid, String roleCode, String type, String name) {
        String roleBid = apmRoleService.getSpaceRoleBid(spaceBid, roleCode);
        if(StringUtils.isBlank(roleBid)){
            return Collections.emptyList();
        }
        //获取所有的角色
        List<String> allChildBid = apmRoleService.getAllChildBid(roleBid);
        return new ArrayList<>(allChildBid.parallelStream().map(bid -> listByRoleBid(bid, type, name)).flatMap(Collection::stream)
                .collect(Collectors.toMap(ApmRoleIdentityVO::getEmployeeNo, Function.identity(), (v1, v2) -> v1))
                .values());
    }

    @Override
    public List<ApmRoleIdentityVO> listByRole(ApmRoleQO apmRoleQO) {
        // 全部成员特殊处理
        List<ApmRoleIdentity> apmRoleIdentityList;
        // 全局系统角色从配置中心捞人
        if (RoleConst.SYS_GLOBAL_ROLE_TYPE.contains(apmRoleQO.getBizType())) {
            return listSysUser(apmRoleQO.getCode());
        }
        if (RoleConstant.SPACE_ALL_EN.equals(apmRoleQO.getCode())) {
            apmRoleIdentityList = apmRoleIdentityService.listAllBySphereBid(apmRoleQO.getSphereBid(), apmRoleQO.getBizType(), apmRoleQO.getName());
        } else {
            apmRoleIdentityList = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery().eq(ApmRoleIdentity::getRoleBid, apmRoleQO.getBid())
                    .eq(ApmRoleIdentity::getType, apmRoleQO.getBizType())
                    .like(StringUtils.isNotBlank(apmRoleQO.getName()), ApmRoleIdentity::getName, apmRoleQO.getName())
                    .eq(ApmRoleIdentity::getDeleteFlag, 0)
                    .orderByAsc(ApmRoleIdentity::getCreatedTime)
            );
        }
        List<ApmRoleIdentityVO> resList = ApmRoleIdentityConerter.INSTANCE.entityList2VOList(apmRoleIdentityList);
        for (ApmRoleIdentityVO apmRoleIdentityVO : resList) {
            if (apmRoleIdentityVO.getType().equals(TypeEnum.EMPLOYEE.getCode())) {
                apmRoleIdentityVO.setEmployeeName(apmRoleIdentityVO.getName());
                apmRoleIdentityVO.setEmployeeNo(apmRoleIdentityVO.getIdentity());
            } else {
                apmRoleIdentityVO.setDeptNo(apmRoleIdentityVO.getIdentity());
                apmRoleIdentityVO.setDeptName(apmRoleIdentityVO.getName());
            }
        }
        return resList;
    }

    @Override
    public Boolean personMonthInputStat() {
        //获取上一个月月份(因为是每个月一号统计上一个月的数据)
        String month = DateUtil.format(DateUtils.addMonths(new Date(), -1), "yyyy-MM");
        //查询所有项目
        com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper();
        qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0);
        QueryCondition queryCondition = new QueryCondition();
        List<com.transcend.plm.datadriven.api.model.QueryWrapper> queryWrappers = com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo);
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(1);
        pageQo.setSize(1);
        PagedResult<MObject> page = objectModelCrudI.page(PROJECT_MODEL_CODE, pageQo, true);
        if (page == null || page.getData().isEmpty()) {
            return true;
        }
        long total = page.getTotal();
        int pageSize = 10000;
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            pageQo.setCurrent(i);
            pageQo.setSize(pageSize);
            page = objectModelCrudI.page(PROJECT_MODEL_CODE, pageQo, true);
            List<MObject> mObjectList = page.getData();
            if (CollectionUtils.isEmpty(mObjectList)) {
                return true;
            }
            saveMemberInputStat(mObjectList, month);
        }
        return true;
    }

    private Boolean saveMemberInputStat(List<MObject> mObjectList, String month) {
        Map<String, MObject> projectMap = mObjectList.stream().collect(Collectors.toMap(MObject::getBid, Function.identity(), (k1, k2) -> k1));
        Set<String> projectBids = mObjectList.stream().map(MObject::getBid).collect(Collectors.toSet());
        //查询sphereBids
        List<ApmSphere> apmSphereList = apmSphereService.getByBizBids(TypeEnum.INSTANCE.getCode(), projectBids);
        Map<String, String> apmSphereBidMap = apmSphereList.stream().collect(Collectors.toMap(ApmSphere::getBid, ApmSphere::getBizBid));
        if (CollectionUtils.isEmpty(apmSphereList)) {
            return true;
        }
        Set<String> apmSphereBids = apmSphereList.stream().map(ApmSphere::getBid).collect(Collectors.toSet());
        List<ApmRoleIdentity> apmRoleIdentities = apmRoleIdentityService.listInputPercentageNotNull(apmSphereBids);
        if (CollectionUtils.isEmpty(apmRoleIdentities)) {
            return true;
        }
        List<MemberInputStat> addList = new ArrayList<>();
        Map<String, String> jobNumberMap = apmRoleIdentities.stream().collect(Collectors.toMap(ApmRoleIdentity::getIdentity, v -> Optional.ofNullable(v.getName()).orElse(""), (k1, k2) -> k1));
        Map<String, Map<String, Integer>> collect = apmRoleIdentities.stream().collect(Collectors.groupingBy(ApmRoleIdentity::getSphereBid, Collectors.groupingBy(ApmRoleIdentity::getIdentity, Collectors.summingInt(ApmRoleIdentity::getInputPercentage))));
        for (Map.Entry<String, Map<String, Integer>> entry : collect.entrySet()) {
            String sphereBid = entry.getKey();
            Map<String, Integer> value = entry.getValue();
            String bizBid = apmSphereBidMap.get(sphereBid);
            MObject mObject = projectMap.get(bizBid);
            for (Map.Entry<String, Integer> entry1 : value.entrySet()) {
                String jobNumber = entry1.getKey();
                Integer inputPercentage = entry1.getValue();
                MemberInputStat memberInputStat = new MemberInputStat();
                memberInputStat.setMonth(month);
                memberInputStat.setJobNumber(jobNumber);
                memberInputStat.setInputPercentage(inputPercentage);
                memberInputStat.setBizBid(bizBid);
                memberInputStat.setName(jobNumberMap.get(jobNumber));
                memberInputStat.setCreatedTime(new Date());
                if (mObject != null) {
                    if (!ObjectUtils.isEmpty(mObject.get(SPACE_BID.getCode()))) {
                        memberInputStat.setSpaceBid(mObject.get(SPACE_BID.getCode()).toString());
                    }
                    if (!ObjectUtils.isEmpty(mObject.get(SPACE_APP_BID.getCode()))) {
                        memberInputStat.setSpaceAppBid(mObject.get(SPACE_APP_BID.getCode()).toString());
                    }
                }
                addList.add(memberInputStat);
            }
        }
        memberInputStatService.remove(new QueryWrapper<MemberInputStat>().eq("month", month));
        memberInputStatService.saveBatch(addList);
        return null;
    }

    /**
     * 获取系统用户
     *
     * @param code
     * @return
     */
    @NotNull
    private List<ApmRoleIdentityVO> listSysUser(String code) {
        List<CfgRoleUserVo> sysUsers = cfgRoleFeignClient.listUsersByRoleCode(code).getCheckExceptionData();
        if (CollectionUtils.isEmpty(sysUsers)) {
            return Lists.newArrayList();
        }
        return sysUsers.stream().map(sysUser -> {
            ApmRoleIdentityVO apmRoleIdentityVO = new ApmRoleIdentityVO();
            apmRoleIdentityVO.setEmployeeName(sysUser.getUserName());
            apmRoleIdentityVO.setEmployeeNo(sysUser.getJobNumber());
            return apmRoleIdentityVO;
        }).collect(Collectors.toList());
    }

    /**
     * 通知根据角色编码找相关人员
     *
     * @param notifyRoleCodes
     * @param instanceBid
     * @param spaceAppBid
     * @param spaceBid
     * @return
     */
    @Override
    public List<String> listNotifyJobNumbers(List<String> notifyRoleCodes, String instanceBid, String spaceAppBid, String spaceBid) {
        //先查实例角色
        List<String> jobNumbers = getJobNumbers(instanceBid, TypeEnum.INSTANCE.getCode(), notifyRoleCodes);
        if (CollectionUtils.isEmpty(jobNumbers)) {
            //查询流程角色
            ApmSphere apmSphereInstance = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
            if (apmSphereInstance != null) {
                List<ApmRoleVO> apmRoleVOS = apmRoleService.listByRoleBidsByCodes(notifyRoleCodes, apmSphereInstance.getBid());
                if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                    List<String> roleBids = apmRoleVOS.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
                    List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndRoles(instanceBid, roleBids);
                    if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)) {
                        jobNumbers = apmFlowInstanceRoleUsers.stream().map(ApmFlowInstanceRoleUser::getUserNo).distinct().collect(Collectors.toList());
                    }
                }
            }
        }
        /*if (CollectionUtils.isEmpty(jobNumbers)) {
            // 查应用角色
            jobNumbers = getJobNumbers(spaceAppBid, TypeEnum.OBJECT.getCode(), notifyRoleCodes);
            if (CollectionUtils.isEmpty(jobNumbers)) {
                // 查空间角色
                jobNumbers = getJobNumbers(spaceBid, TypeEnum.SPACE.getCode(), notifyRoleCodes);
            }
        }*/
        return jobNumbers;
    }


    private List<String> getJobNumbers(String bizBid, String type, List<String> codes) {
        if (StringUtils.isEmpty(bizBid) || CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        ApmSphere apmSphereInstance = apmSphereService.getByBizBidAndType(bizBid, type);
        if (apmSphereInstance != null) {
            List<ApmRoleVO> apmRoleVOS = apmRoleService.listByRoleBidsByCodes(codes, apmSphereInstance.getBid());
            if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                List<String> roleBids = apmRoleVOS.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
                List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery().in(ApmRoleIdentity::getRoleBid, roleBids)
                        .eq(ApmRoleIdentity::getType, "employee")
                        .eq(ApmRoleIdentity::getDeleteFlag, 0)
                        .orderByAsc(ApmRoleIdentity::getCreatedTime));
                if (CollectionUtils.isNotEmpty(apmRoleIdentityList)) {
                    return apmRoleIdentityList.stream().map(ApmRoleIdentity::getIdentity).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> listByRoleBids(List<String> roleBids, String type) {
        List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentityService.list(Wrappers.<ApmRoleIdentity>lambdaQuery().in(ApmRoleIdentity::getRoleBid, roleBids)
                .eq(ApmRoleIdentity::getType, type)
                .eq(ApmRoleIdentity::getDeleteFlag, 0)
                .orderByAsc(ApmRoleIdentity::getCreatedTime));
        //判断查询结果不为空
        if (CollectionUtils.isEmpty(apmRoleIdentityList)) {
            // 返回用户工集合
            return apmRoleIdentityList.stream().map(ApmRoleIdentity::getIdentity).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void copyRoleAndIdentity(ApmCopyRoleAndIdentityQo qo) {
        // 查询当前空间sphere
        ApmSphere targetSphere = Optional.ofNullable(apmSphereService.getByBizBidAndType(qo.getTargetBid(), qo.getType())).orElseThrow(() -> new BusinessException("当前资源不存在"));
        // 查询所选空间sphere
        ApmSphere sourceSphere = Optional.ofNullable(apmSphereService.getByBizBidAndType(qo.getSourceBid(), qo.getType())).orElseThrow(() -> new BusinessException("所选资源不存在"));
        // 查询角色和成员 left join
        List<ApmRoleIdentity> sourceRoleAndIdentityList = apmRoleIdentityService.listAllRoleAndIdentityBySphereBid(sourceSphere.getBid());
        if (CollectionUtils.isEmpty(sourceRoleAndIdentityList)) {
            return;
        }
        // 按照roleCode分组，并且去掉角色下没有成员的数据
        Map<String, List<ApmRoleIdentity>> sourceRoleCodeWithIdentityMap = sourceRoleAndIdentityList.stream().collect(Collectors.groupingBy(ApmRoleIdentity::getRoleCode,
                Collectors.collectingAndThen(Collectors.toList(), m -> m.stream().filter(e -> StringUtils.isNotBlank(e.getIdentity())).collect(Collectors.toList()))));
        List<ApmRoleIdentity> targetRoleAndIdentityList = apmRoleIdentityService.listAllRoleAndIdentityBySphereBid(targetSphere.getBid());
        Map<String, List<ApmRoleIdentity>> targetRoleCodeWithIdentityMap = targetRoleAndIdentityList.stream()
                .collect(Collectors.groupingBy(ApmRoleIdentity::getRoleCode,
                        Collectors.collectingAndThen(Collectors.toList(), m -> m.stream().filter(e -> StringUtils.isNotBlank(e.getIdentity())).collect(Collectors.toList()))));
        // 当前资源 和所选资源的角色code与角色信息映射关系
        Map<String, ApmRoleIdentity> sourceRoleCodeWithRoleNameMap = sourceRoleAndIdentityList.stream()
                .collect(Collectors.toMap(ApmRoleIdentity::getRoleCode, Function.identity(), (k1, k2) -> k1.getRoleBid().compareTo(k2.getRoleBid()) > 0 ? k1 : k2));
        Map<String, ApmRoleIdentity> targetRoleCodeWithRoleNameMap = targetRoleAndIdentityList.stream()
                .collect(Collectors.toMap(ApmRoleIdentity::getRoleCode, Function.identity(), (k1, k2) -> k1.getRoleBid().compareTo(k2.getRoleBid()) > 0 ? k1 : k2));
        // 当前资源 需要新增的角色
        List<ApmRole> addRoleList = Lists.newArrayList();
        // 当前资源 需要新增的成员
        List<ApmRoleIdentity> addRoleIdentityList = Lists.newArrayList();
        sourceRoleCodeWithIdentityMap.forEach((roleCode, roleIdentityList) -> {
            // 如果当前资源下 角色存在，则看成员是否需要新增
            if (targetRoleCodeWithIdentityMap.containsKey(roleCode)) {
                Map<String, ApmRoleIdentity> sourceIdentityMap = roleIdentityList.stream().collect(Collectors.toMap(ApmRoleIdentity::getIdentity, Function.identity(), (k1, k2) -> k1));
                List<ApmRoleIdentity> targetRoleIdentityList = targetRoleCodeWithIdentityMap.get(roleCode);
                // 所选资源下,该角色成员不为空
                if (CollectionUtils.isNotEmpty(sourceIdentityMap)) {
                    Set<String> targetIdentityList = CollectionUtils.isEmpty(targetRoleIdentityList) ?
                            Sets.newHashSet() : targetRoleIdentityList.stream().map(ApmRoleIdentity::getIdentity).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                    // 找出需要新增的成员
                    Set<String> sourceIdentityList = sourceIdentityMap.keySet();
                    sourceIdentityList.removeAll(targetIdentityList);
                    if (CollectionUtils.isNotEmpty(sourceIdentityList)) {
                        sourceIdentityList.forEach(identity -> {
                            String roleBid = targetRoleCodeWithRoleNameMap.get(roleCode).getRoleBid();
                            ApmRoleIdentity addRoleIdentity = sourceIdentityMap.get(identity);
                            addRoleIdentity.setId(null).setRoleBid(roleBid).setCreatedTime(new Date()).setCreatedBy(SsoHelper.getJobNumber());
                            addRoleIdentityList.add(addRoleIdentity);
                        });
                    }
                }
                // 所选资源下,该角色成员为空,不处理
            } else {
                // 如果当前资源下 角色不存在，先在当前资源下新增角色，再新增成员
                String roleBid = SnowflakeIdWorker.nextIdStr();
                ApmRoleIdentity apmRoleIdentity = sourceRoleCodeWithRoleNameMap.get(roleCode);
                // 添加新增角色
                String parentBid = "0";
                if (StringUtils.isNotEmpty(apmRoleIdentity.getParentBid())) {
                    parentBid = apmRoleIdentity.getParentBid();
                }
                addRoleList.add(new ApmRole()
                        .setCode(roleCode)
                        .setType(apmRoleIdentity.getRoleType())
                        .setName(apmRoleIdentity.getRoleName())
                        .setDescription(apmRoleIdentity.getDescription())
                        .setBid(roleBid)
                        .setPbid(RoleConstant.ROOT_BID)
                        .setPath(RoleConstant.ROOT_BID + RoleConstant.SEMICOLON + roleBid)
                        .setSphereBid(targetSphere.getBid())
                        .setCreatedBy(SsoHelper.getJobNumber())
                        .setUpdatedBy(SsoHelper.getJobNumber())
                        .setParentBid(parentBid)
                        .setEnableFlag(CommonConst.ENABLE_FLAG_ENABLE));
                // 添加新增角色成员
                roleIdentityList.forEach(roleIdentity -> addRoleIdentityList.add(roleIdentity.setRoleBid(roleBid).setId(null)
                        .setCreatedTime(new Date()).setCreatedBy(SsoHelper.getJobNumber())));
            }
        });
        transactionTemplate.execute(status -> {
            // 批量新增角色
            if (CollectionUtils.isNotEmpty(addRoleList)) {
                apmRoleService.saveBatch(addRoleList);
            }
            // 批量新增角色成员
            if (CollectionUtils.isNotEmpty(addRoleIdentityList)) {
                apmRoleIdentityService.saveBatch(addRoleIdentityList);
            }
            return true;
        });
    }

    @Override
    public boolean remove(ApmRoleIdentityDto apmRoleIdentityDto) {
        if (Objects.isNull(apmRoleIdentityDto)) {
            return false;
        }
        ApmRoleIdentity apmRoleIdentity = ApmRoleIdentityConerter.INSTANCE.dto2Entity(apmRoleIdentityDto);
        return apmRoleIdentityService.removeByCondition(apmRoleIdentity, apmRoleIdentityDto.getRoleBids());
    }
}