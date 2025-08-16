package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.tool.SecrecyWrapperHandler;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum.SPACE_BID;

/**
 * 多对象数据查询执行者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/7 15:10
 */
@Service
@AllArgsConstructor
public class MultiTreeDataQueryExecutor {
    private ObjectModelStandardI<MObject> objectModelCrudI;
    private IAppDataService appDataService;
    private ApmSpaceAppService apmSpaceAppService;
    private ApmRoleDomainService apmRoleDomainService;
    private ApmRoleService apmRoleService;

    /**
     * 执行多对象数据查询
     *
     * @param params 查询参数
     * @return 查询结果
     */
    @Nonnull
    public List<MObject> execute(final Params params) {
        // 1、是否需要过滤开发权限 特殊逻辑，需要看用户是否在开发管理角色下
        if (Boolean.TRUE.equals(params.developmentPermission)) {
            if (!hasDevelopmentPermissions(params.empNo)) {
                return Collections.emptyList();
            }
        }

        //开始组装条件
        List<QueryWrapper> wrappers = new ArrayList<>(params.wrappers);

        // 2、是否需要过滤空间
        if (Boolean.TRUE.equals(params.instanceQueryFilterSpace)) {
            addAndExpression(wrappers);
            wrappers.addAll(getSpaceBidWrappers(params.spaceBid, params.isRelation));
        }

        //3、添加保密权限过滤
        if (!params.isRelation) {
            wrappers = SecrecyWrapperHandler.autoAddSecrecyWrapper(wrappers, params.modelCode, params.empNo);
        }

        //4、确定是否需要权限
        boolean isPermission;
        if (params.isRelation || Boolean.FALSE.equals(params.defaultCheckPermission)) {
            isPermission = false;
        } else {
            isPermission = Boolean.TRUE.equals(Optional.ofNullable(params.instanceQueryPermission)
                    .orElse(params.defaultCheckPermission));
        }

        //权限需要空间应用支持，故需要先查询空间应用
        ApmSpaceApp spaceApp = null;
        if (isPermission) {
            spaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(params.spaceBid, params.modelCode);
            isPermission = spaceApp != null;
        }

        //执行查询
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setQueries(wrappers);
        List<MObject> list = isPermission ?
                //需要权限查询方式
                appDataService.list(params.spaceBid, spaceApp.getBid(), params.empNo, queryCondition) :
                //不需要权限查询方式
                objectModelCrudI.list(params.modelCode, queryCondition);

        return Optional.ofNullable(list).orElseGet(Collections::emptyList);

    }


    /**
     * 添加And表达式
     *
     * @param wrappers 查询条件
     */
    private static void addAndExpression(List<QueryWrapper> wrappers) {
        if (!wrappers.isEmpty()) {
            wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        }
    }

    /**
     * 获取空间Bid查询条件
     *
     * @param spaceBid   空间Bid
     * @param isRelation 是否关联模型
     * @return 查询条件
     */
    @NotNull
    private List<QueryWrapper> getSpaceBidWrappers(String spaceBid, boolean isRelation) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq(SPACE_BID.getCode(), spaceBid);
        //非关联模型可匹配挂载空间
        if (!isRelation) {
            wrapper.or().eq(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode(), spaceBid);
        }
        return QueryWrapper.buildSqlQo(wrapper);
    }

    /**
     * 是否拥有开发权限
     *
     * @param userNo 用户编码
     * @return 是否拥有开发权限
     */
    private boolean hasDevelopmentPermissions(String userNo) {
        List<ApmRole> apmRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(userNo, null);
        if (apmRoles == null || apmRoles.isEmpty()) {
            return false;
        }
        //ALM特殊逻辑 要求用户在该角色下即拥有权限
        List<String> roleList = apmRoleService.getAllChildCode("_DEV_MANAGE");
        Set<String> userRoles = apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toSet());
        return roleList.stream().anyMatch(userRoles::contains);
    }


    /**
     * 数据查询参数
     */
    @Accessors(chain = true)
    public static class Params {

        /**
         * 用户编码
         */
        @Setter
        private String empNo;
        /**
         * 空间Bid
         */
        @Setter
        private String spaceBid;
        /**
         * 默认是否需要权限
         */
        @Setter
        private Boolean defaultCheckPermission;
        /**
         * 模型编码
         */
        @Getter
        private String modelCode;

        /**
         * 是否关联模型
         */
        private boolean isRelation;
        /**
         * 实例查询需要权限
         */
        private Boolean instanceQueryPermission;
        /**
         * 是筛选开发权限
         */
        private Boolean developmentPermission;
        /**
         * 实例查询过滤空间
         */
        private Boolean instanceQueryFilterSpace;

        /**
         * 筛选条件
         */
        private List<QueryWrapper> wrappers = Collections.emptyList();


        public Params setMultiTreeConfig(boolean isRelation, MultiTreeConfigVo config) {
            this.modelCode = isRelation ? config.getRelationModelCode() : config.getSourceModelCode();
            this.isRelation = isRelation;
            if (isRelation) {
                //关系查询不通过权限和空间
                this.instanceQueryPermission = false;
                this.developmentPermission = false;
                this.instanceQueryFilterSpace = false;
            } else {
                this.instanceQueryPermission = config.getInstanceQueryPermission();
                this.developmentPermission = config.getDevelopmentPermission();
                this.instanceQueryFilterSpace = config.getInstanceQueryFilterSpace();
            }
            return this;
        }

        public Params setWrappers(List<QueryWrapper> wrappers) {
            this.wrappers = wrappers == null ? Collections.emptyList() : wrappers;
            return this;
        }

        public static Params ofInstance(MultiTreeConfigVo config, String empNo, String spaceBid,
                                        Boolean defaultCheckPermission, List<QueryWrapper> wrappers) {
            return new Params()
                    .setMultiTreeConfig(false, config)
                    .setEmpNo(empNo)
                    .setSpaceBid(spaceBid)
                    .setDefaultCheckPermission(defaultCheckPermission)
                    .setWrappers(wrappers);
        }

        public static Params ofRelation(MultiTreeConfigVo config, List<QueryWrapper> wrappers) {
            return new Params()
                    .setMultiTreeConfig(true, config)
                    .setDefaultCheckPermission(false)
                    .setWrappers(wrappers);
        }

    }
}
