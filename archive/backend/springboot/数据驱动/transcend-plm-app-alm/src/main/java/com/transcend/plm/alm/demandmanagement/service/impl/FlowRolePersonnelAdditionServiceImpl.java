package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.transcend.plm.alm.demandmanagement.service.FlowRolePersonnelAdditionService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程角色添加处理人操作
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/12 11:29
 */
@Log4j2
@Service
public class FlowRolePersonnelAdditionServiceImpl implements FlowRolePersonnelAdditionService {
    private List<Config> configs;

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private IRuntimeService runtimeService;

    @Autowired
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmRoleDomainService apmRoleDomainService;

    @Resource
    private ApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;


    @Value("${transcend.alm.flow.RolePersonnelAddition:[]}")
    public void setConfigs(String configsJson) {
        try (JSONValidator validator = JSONValidator.from(configsJson)) {
            if (validator.validate()) {
                this.configs = JSON.parseArray(configsJson, Config.class);
                this.configs = this.configs.stream().filter(Objects::nonNull)
                        .filter(config -> StringUtils.isNotBlank(config.getSpaceAppBid())
                                && StringUtils.isNotBlank(config.getFlowNodeWebBid())
                                && StringUtils.isNotBlank(config.getRoleCode())
                                && StringUtils.isNotBlank(config.getFieldName())
                        ).collect(Collectors.toList());
                return;
            }
        } catch (Exception e) {
            log.error("setConfigs configsJson is not valid json", e);
        }
        log.error("setConfigs configsJson is not valid json");
    }


    @Override
    public boolean isSupport(String spaceAppBid) {
        return this.configs.stream().anyMatch(config -> config.getSpaceAppBid().equals(spaceAppBid));
    }

    @Override
    public void execute(ApmSpaceApp spaceApp, String flowNodeBid, Map<String, Object> updateData) {
        if (spaceApp == null) {
            return;
        }
        ApmFlowInstanceNode flowNode = apmFlowInstanceNodeService.getByBid(flowNodeBid);
        if (flowNode == null) {
            return;
        }
        MapWrapper updateMap = Optional.ofNullable(updateData).map(MapWrapper::new).orElseGet(MapWrapper::new);
        this.configs.stream().filter(config -> config.getSpaceAppBid().equals(spaceApp.getBid())
                && config.getFlowNodeWebBid().equals(flowNode.getWebBid())).forEach(config -> {

            //获取实例数据
            List<String> empNoList;
            if (updateMap.containsKey(config.getFieldName())) {
                empNoList = strToList(updateMap.getStr(config.getFieldName()));
            } else {
                empNoList = getInstanceEmpNoList(spaceApp.getModelCode(), flowNode.getInstanceBid(), config.getFieldName());
            }
            //查询角色本身的成员
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceApp.getBid(), TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return;
            }
            ApmRoleVO apmRoleVO = apmRoleService.getByRoleBidsByCode(config.getRoleCode(), spaceSphere.getBid());
            if (apmRoleVO == null) {
                return;
            }
            ApmRoleQO apmRoleQo = new ApmRoleQO();
            apmRoleQo.setBid(apmRoleVO.getBid());
            apmRoleQo.setBizType("employee");
            apmRoleQo.setCode(apmRoleVO.getCode());
            apmRoleQo.setSphereBid(apmRoleVO.getSphereBid());
            List<ApmRoleIdentityVO> apmRoleIdentityVOS = apmRoleIdentityDomainService.listByRole(apmRoleQo);
            List<String> apmRoleUsers = Optional.ofNullable(apmRoleIdentityVOS)
                    .map(list -> list.stream().map(ApmRoleIdentityVO::getEmployeeNo).filter(StringUtils::isNotBlank).collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
            if (CollUtil.isEmpty(apmRoleUsers) && CollUtil.isEmpty(empNoList)) {
                return;
            }
            if (CollectionUtils.isNotEmpty(empNoList)) {
                apmRoleUsers.addAll(empNoList);
            }
            List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
            for (String user : apmRoleUsers) {
                ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                apmFlowInstanceRoleUser.setRoleBid(apmRoleVO.getBid());
                apmFlowInstanceRoleUser.setInstanceBid(flowNode.getInstanceBid());
                apmFlowInstanceRoleUser.setUserNo(user);
                apmFlowInstanceRoleUser.setSpaceBid(spaceApp.getSpaceBid());
                apmFlowInstanceRoleUser.setSpaceAppBid(spaceApp.getBid());
                apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
            }
            try {
                //保存角色
                apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBidsToIds(flowNode.getInstanceBid(), Collections.singletonList(apmRoleVO.getBid()));
                apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            } catch (Exception e) {
                log.error("execute save role employee fail config:{} ", config, e);
            }
        });
    }


    //region private method

    /**
     * 获取实例中工号字段集合
     *
     * @param modelCode   模型编码
     * @param instanceBid 实例数据bid
     * @param fieldName   字段名称
     * @return 工号字段集合
     */
    @Nullable
    private List<String> getInstanceEmpNoList(String modelCode, String instanceBid, String fieldName) {
        return Optional.ofNullable(instanceBid)
                .map(bid -> objectModelCrudI.getByBid(modelCode, bid))
                .map(MapWrapper::new)
                .map(ins -> ins.getStr(fieldName))
                .map(this::strToList).orElse(null);
    }

    /**
     * 字符串转集合
     *
     * @param str 字符串
     * @return 集合
     */
    private List<String> strToList(String str) {
        try {
            List<String> result;
            final String jsonArrayStart = "[";
            final String jsonArrayEnd = "]";
            if (str.startsWith(jsonArrayStart) && str.endsWith(jsonArrayEnd)) {
                result = JSON.parseArray(str, String.class);
            } else {
                result = new ArrayList<>();
                result.add(str);
            }
            return result;
        } catch (Exception e) {
            log.error("strToList error:{}", str, e);
        }
        return null;
    }

    //endregion
}
