package com.transcend.plm.datadriven.apm.strategy;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.tool.SecrecyWrapperHandler;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @Author Qiu Yuhao （Mickey）
 * @Date 2024/3/20 11:30
 * @Describe 分页查询条件客制化策略 针对 空间bid+应用bid 对应 拓展策略
 */
@Slf4j
@Component
public class CustomPageExtendStrategy {

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmRoleDomainService apmRoleDomainService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;

    @Value("${custom.page.extend.demandPool:}")
    private String demandPool;

    @Value("${custom.page.extend.rolePermissionsList:}")
    private List<String> rolePermissionsList;

    private Map<String, BiConsumer<ApmSpaceApp, BaseRequest<QueryCondition>>> preStrategyMap;
    private Map<String, BiConsumer<ApmSpaceApp, PagedResult<MObject>>> postStrategyMap;
//    ThreadLocal<QueryCondition> threadLocalQueryCondition = new ThreadLocal<>();

    
    private final String DEMAND_POOL_SQL = "(EXISTS (SELECT 1 from apm_flow_instance_role_user r WHERE r.`instance_bid` = a.bid and r.`user_no` = '%s') OR a.created_by = '%s')";

    @PostConstruct
    public void init() {
        preStrategyMap = new HashMap<>(16);
        postStrategyMap = new HashMap<>(16);
        preStrategyMap.put(demandPool, this::demandPoolPreHandler);
        preStrategyMap.put("modelCodeA01", this::addConfidentialityCondition);
    }

    public void preHandler(String spaceAppBid, BaseRequest<QueryCondition> pageQo) {
        ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
        if (app == null) {
            return;
        }
        //ALM 特殊逻辑，添加保密条件
        addConfidentialityCondition(app, pageQo);

        String strategyKey = app.getSpaceBid() + app.getBid();
        BiConsumer<ApmSpaceApp, BaseRequest<QueryCondition>> strategy = preStrategyMap.get(strategyKey);
        if (strategy == null) {
            return;
        }
        strategy.accept(app, pageQo);
    }

    private void demandPoolPreHandler(ApmSpaceApp apmSpaceApp, BaseRequest<QueryCondition> queryConditionBaseRequest) {
        // 获取当前用户的工号
        String jobNumber = SsoHelper.getJobNumber();
        // 首先校验当前登录用户是否是拥有最大权限的用户
        if (Boolean.TRUE.equals(checkIsSuperUser(apmSpaceApp, jobNumber))) {
            return;
        }
        // 如果不是权限最大用户，则只让用户看到 1.自己创建的需求 2.自己参与的需求
        buildGeneralUserWrapper(queryConditionBaseRequest, jobNumber);
    }

    /**
     * 条件保密条件
     *
     * @param apmSpaceApp 应用信息
     * @param request     请求参数
     */
    private void addConfidentialityCondition(ApmSpaceApp apmSpaceApp, BaseRequest<QueryCondition> request) {
        QueryCondition condition = request.getParam();
        condition.setQueries(SecrecyWrapperHandler.autoAddSecrecyWrapper(
                condition.getQueries(), apmSpaceApp.getModelCode(), SsoHelper.getJobNumber()));
    }

    private void buildGeneralUserWrapper(BaseRequest<QueryCondition> queryConditionBaseRequest, String jobNumber) {
        List<QueryWrapper> wrappers = queryConditionBaseRequest.getParam().getQueries();
        QueryWrapper andWrapper = new QueryWrapper(Boolean.TRUE);
        andWrapper.setSqlRelation(" and ");
        QueryWrapper sqlWrapper = new QueryWrapper();
        sqlWrapper.setSqlRelation(String.format(DEMAND_POOL_SQL, jobNumber, jobNumber));
        wrappers.add(andWrapper);
        wrappers.add(sqlWrapper);
        queryConditionBaseRequest.getParam().setQueries(wrappers);
    }

    private Boolean checkIsSuperUser(ApmSpaceApp apmSpaceApp, String jobNumber) {
        // 判断是否是系统超级管理员
        if (Boolean.TRUE.equals(apmRoleDomainService.isGlobalAdmin())) {
            return Boolean.TRUE;
        }
        // 查询当前角色在空间下有哪些角色
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(apmSpaceApp.getSpaceBid(), TypeEnum.SPACE.getCode());
        List<ApmRole> roleList = apmRoleService.getRoleListByJobNumAndSphereBid(jobNumber, spaceSphere.getBid());
        // 这里不会出现角色列表为空的情况，想要看到空间，就必须在某个空间角色下
        // 通过权限对角色过滤 是否是空间管理员或者是否是某些特定角色
        return roleList.stream().anyMatch(role -> Objects.equals(role.getCode(), RoleConstant.SPACE_ADMIN_EN) || rolePermissionsList.contains(role.getCode()));
    }


}
