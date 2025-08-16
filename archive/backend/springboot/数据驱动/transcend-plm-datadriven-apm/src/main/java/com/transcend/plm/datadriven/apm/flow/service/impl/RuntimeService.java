package com.transcend.plm.datadriven.apm.flow.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.feign.LifeCycleFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.api.model.lifecycle.dto.TemplateDto;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.VersionObjectEnum;
import com.transcend.plm.datadriven.apm.constants.*;
import com.transcend.plm.datadriven.apm.enums.FlowEnum;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventHandlerFactory;
import com.transcend.plm.datadriven.apm.flow.event.IFlowEventHandler;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCompleteCheckEvent;
import com.transcend.plm.datadriven.apm.flow.maspstruct.AmpFlowTemplateNodeConerter;
import com.transcend.plm.datadriven.apm.flow.maspstruct.ApmFlowInstanceNodeConverter;
import com.transcend.plm.datadriven.apm.flow.pi.service.PiProductBtConfigService;
import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowInstanceProcessDto;
import com.transcend.plm.datadriven.apm.flow.pojo.dto.NodeExecuteBusEvent;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.flow.pojo.type.FlowTypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.*;
import com.transcend.plm.datadriven.apm.flow.repository.po.*;
import com.transcend.plm.datadriven.apm.flow.service.*;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateFlowTodoDriveAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskAO;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import com.transcend.plm.datadriven.apm.task.mapper.ApmTaskMapper;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.enums.LanguageEnum;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.service.IUacUserService;
import com.transsion.sdk.push.domain.message.element.Button;
import com.transsion.sdk.push.domain.message.element.Text;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.BUS_MODLUE_MODEL_CODE;
import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.SSRMP_MODEL_CODE;
import static com.transcend.plm.datadriven.apm.enums.CommonEnum.PER_ROLE_CODE;
import static com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum.SPACE_APP_BID;
import static com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum.SPACE_BID;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.MemberDemandDistributionChartConstant.PRODUCT_OWNER;

/**
 * @author peng.qin
 * @date 2024/07/24
 */
@Service
@Slf4j
public class RuntimeService implements IRuntimeService, InitializingBean {
    @Resource
    private ApmFlowApplicationService apmFlowApplicationService;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;
    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;
    @Resource
    private ApmFlowNodeEventService apmFlowNodeEventService;

    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Resource
    private OperationLogEsService operationLogEsService;
    @Resource
    private ApmFlowInstanceProcessService apmFlowInstanceProcessService;

    @Resource
    private ApmFlowNodeLineService apmFlowNodeLineService;

    @Resource
    private LifeCycleFeignClient lifeCycleFeignClient;

    public static String IrModelCode = "A5G";

    @Resource
    private ApmFlowTemplateNodeService apmFlowTemplateNodeService;

    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    private IApmRoleDomainService apmRoleService;

    @Resource
    private ApmRoleService roleService;

    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;

    @Resource
    IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;
    @Resource
    private PiProductBtConfigService piProductBtConfigService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IToDoCenterService toDoCenterService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private IUacUserService uacUserService;

    @Resource
    private DictionaryFeignClient dictionaryFeignClient;
    @Resource
    private ApmTaskMapper apmTaskMapper;

    /**
     * 一级部门
     */

    private static final String FIST_LEVEL_DEPARTMENT = "firstLevelDepartmentToWhichTheBusinessBelon";

    /**
     * 二级部门
     */
    private static final String SECONDARY_DEPARTMENT = "secondaryDepartmentToWhichTheBusinessBelongs";
    /**
     * 业务负责人
     */
    private static final String PRODUCTOWNER = "productOwner";

    /**
     * 业务负责人
     */
    private static final String BUSINESSMANAGER = "businessManager";

    @Value("${apm.web.url:https://alm.transsion.com/#}")
    private String apmWebUrl;
    /**
     * 依次为空间bid，应用bid，实例bid
     */
    @Value("${apm.instance.web.path:/share/info/%s/%s/%s?viewMode=tableView}")
    private String apmInstanceWebPathTemplate;
//    @Value("${apm.msg.picture.url:https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1702983291787/20231219-185031.png}")
    @Value("${apm.msg.picture.url:https://transsion-platform02.oss-cn-shenzhen.aliyuncs.com/ipm/doc/files/1732507311154/20241125-115504-new.jpg}")
    private String apmMsgPictureUrl;
    @Value("${apm.flow.todoCenter.appName:Transcend}")
    private String todoCenterAppName;

    @Value("${customer.flow.bid:1197602869796229120}")
    private String customerFlowBid;
    private String pass = "Y";

    @Value("${flow.node.skip.wait.set:SRE过滤,RMT评估,完成节点,领域负责人分发}")
    private Set<String> skipWaitSet;

    @Value("${transcend.plm.apm.special.flowTemplateBids:1197874180675608576,1197602869796229120}")
    private Set<String> specialFlowTemplateBidSet;

    @Value("${transcend.plm.apm.special.spaceAppBid:1197585980276293632,1195047614899425280}")
    private Set<String> specialSpaceAppBidSet;

    @Value("${transcend.plm.apm.special.rrSpaceAppBid:1253656300279398400}")
    private String rrSpaceAppBid;

    @Value("${transcend.plm.apm.special.irSpaceAppBid:1253665116014346240}")
    private String irSpaceAppBid;

    @Value("${transcend.plm.apm.special.piSpecialSpaceAppBid:1258356418957418496}")
    private String piSpecialFlowSpaceAppBid;

    @Value("${transcend.plm.apm.flow.task.notify:false}")
    private boolean flowTaskNotify;

    @Value("${transcend.plm.apm.flow.task.notifyCenter:false}")
    private boolean flowTaskNotifyCenter;

    /**
     * 主管角色编码
     */
    @Value("${transcend.plm.apm.special.managerRoleCode:Directsupervisor}")
    private String managerRoleCode;

    @Value("${transcend.plm.apm.special.manufacturingCenterBusiUser:18652659}")
    private String manufacturingCenterBusiUser;

    @Value("${transcend.plm.apm.special.customerServiceCenterBusiUser:18651509}")
    private String customerServiceCenterBusiUser;
    /**
     * 业务接口人角色编码
     */
    @Value("${transcend.plm.apm.special.businessInterfacePersonRoleCode:BusinessInterfacePerson}")
    private String businessInterfacePersonRoleCode;

    /**
     * 产品线负责任人 角色编码
     */
    @Value("${transcend.plm.apm.special.productLineResponsiblePerson:ProductLineResponsiblePerson}")
    private String productLineResponsiblePersonRoleCode;

    @Value("${transcend.plm.apm.special.businessManagerRoleCode:BusinessManager}")
    private String businessManagerRoleCode;

    @Value("${transcend.plm.apm.special.productmanagerRoleCode:productmanager}")
    private String productmanagerRoleCode;
    /**
     * 领域modelCode
     */
    @Value("${transcend.plm.apm.domain.modelCode:A4L}")
    private String domainModelCode;

    /**
     * 模块modelCode
     */
    @Value("${transcend.plm.apm.moudle.modelCode:A3D}")
    private String moudleModelCode;


    /**
     * RR原始需求流程 领域产品规划代表角色bid
     */
    @Value("${transcend.plm.apm.special.rrflow.domainProduct.role.code:domainLeader}")
    private String analysisRRDomainRoleCode;

    /**
     * RR原始需求流程 产品责任人角色bid
     */
    @Value("${transcend.plm.apm.special.rrflow.product.role.code:pdm}")
    private String analysisRRProductRoleCode;

    /**
     * 领域产品负责人字段
     */
    @Value("${transcend.plm.apm.domain.productManager:productManagers}")
    private String productManager;
    /**
     * 领域领域开发代表
     */
    @Value("${transcend.plm.apm.domain.domainDevelopmenRepresentative:domainDevelopmenRepresentative}")
    private String domainDevelopmenRepresentative;
    /**
     * 领域负责人角色编码
     */
    @Value("${transcend.plm.apm.domain.domainLeaderCode:softwareproductplanning}")
    private String domainLeaderCode;

    /**
     * 需要解析的其他领域编码，多个逗号分割
     */
    @Value("${transcend.plm.apm.domain.otherDomainCode:sre}")
    private String otherDomainCode;


    /**
     * 硬件实现责任人
     */
    @Value("${transcend.plm.apm.domain.hardwareLmplementationResponsiblePerson:hardwareLmplementationResponsiblePerson}")
    private String hardwareLmplementationResponsiblePerson;
    /**
     * 硬件责任人角色
     */
    @Value("${transcend.plm.apm.domain.hardwareresponsiblepersonCode:Hardwareresponsibleperson}")
    private String hardwareresponsiblepersonCode;
    /**
     * 硬件接口人
     */
    @Value("${transcend.plm.apm.domain.hardwareInterfacePersonnel:hardwareInterfacePersonnel}")
    private String hardwareInterfacePersonnel;
    /**
     * 硬件接口人角色
     */
    @Value("${transcend.plm.apm.domain.hardwareinterfacepersonnelCode:Hardwareinterfacepersonnel}")
    private String hardwareinterfacepersonnelCode;
    /**
     * 需要解析的其他领域编码对应的字段，和上门一一对应，多个逗号分割
     */
    @Value("${transcend.plm.apm.domain.otherDomainAttribute:requirementManagementRepresentative}")
    private String otherDomainAttribute;

    /**
     * 领域SE角色编码
     */
    @Value("${transcend.plm.apm.domain.domainSeCode:domainse}")
    private String domainSeCode;

    /**
     * 角色人员需要轮询的编码
     */
    @Value("${transcend.plm.apm.domain.pollingCode:spp}")
    private String pollingCode;

    /**
     * 模块产品负责人字段
     */
    @Value("${transcend.plm.apm.moudle.productManager:personResponsible}")
    private String moudleProductManager;

    @Value("${transcend.plm.apm.peculiarity.flowTemplateBid:1215292609580322816}")
    private String peculiarityFlowTemplateBid;

    @Value("${transcend.plm.apm.special.domainPlanRepresentRoleCode:Domain_Plan_Represent}")
    private String domainPlanRepresentRoleCode;

    @Value("${transcend.plm.apm.special.sreRoleCode:sre}")
    private String sreRoleCode;

    @Value("${transcend.plm.apm.special.pdmRoleCode:pdm}")
    private String pdmRoleCode;

    private Map<String, String> getOtherDomainCodeMap() {
        Map<String, String> map = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (StringUtils.isNotBlank(otherDomainCode) && StringUtils.isNotBlank(otherDomainAttribute)) {
            String[] codes = otherDomainCode.split(",");
            String[] attributes = otherDomainAttribute.split(",");
            for (int i = 0; i < codes.length; i++) {
                map.put(codes[i], attributes[i]);
            }
        }
        return map;
    }

    /**
     * 处理流程
     *
     * @param templateBid
     * @param instanceBid
     * @param qo
     * @param mObject
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startProcess(String templateBid, String instanceBid, ApmFlowQo qo, MObject mObject) {
        //拷贝模板
        List<ApmFlowTemplateNodeVO> apmFlowTemplateNodes = apmFlowApplicationService.listCurrentTemplateNodes(templateBid);
        if (CollectionUtils.isEmpty(apmFlowTemplateNodes)) {
            return Boolean.TRUE;
        }
        //查看实例是否已经存在
        List<ApmFlowInstanceNode> apmExistedFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isNotEmpty(apmExistedFlowInstanceNodes)) {
            throw new PlmBizException("流程实例已经存在");
        }
        try {
            List<String> allRoleBids = new ArrayList<>();
            for(ApmFlowTemplateNodeVO apmFlowTemplateNodeVO: apmFlowTemplateNodes) {
                try{
                    List<String> nodeRoleBids = apmFlowTemplateNodeVO.getNodeRoleBids();
                    List<String> complateRoleBids =apmFlowTemplateNodeVO.getComplateRoleBids();
                    if(CollectionUtils.isNotEmpty(nodeRoleBids)) {
                        allRoleBids.addAll(nodeRoleBids);
                    }
                    if (CollectionUtils.isNotEmpty(complateRoleBids)) {
                        allRoleBids.addAll(complateRoleBids);
                    }
                }catch (Exception ex){
                    throw new PlmBizException("流程节点配置异常");
                }
            }
            if(allRoleBids.contains(InnerRoleEnum.CREATER.getCode())){
                List<ApmRoleUserAO> roleUserList = qo.getRoleUserList();
                ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(InnerRoleEnum.CREATER.getCode(), SsoHelper.getJobNumber(), null);
                roleUserList.add(apmRoleUserAO);
                qo.setRoleUserList(roleUserList);
            }
        }catch (Exception e){
            log.error("流程创建角色处理异常",e);
        }
        log.info("生成节点实例，以及给角色赋值人员");
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = generateInstanceNodesFromTemplate(instanceBid, apmFlowTemplateNodes);
        //激活第一个节点
        log.info("激活第一个节点");
        ApmFlowInstanceNode startNode = null;
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType())) {
                apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.ACTIVE);
            }
        }
        List<ApmFlowInstanceNode> nextAutoNodes = new ArrayList<>();
        //批量创建InstanceRoleUser
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = generateFlowInstanceRoleUsers(instanceBid, qo);
        apmFlowInstanceNodeService.saveBatch(apmFlowInstanceNodes);
        apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
        //激活第一个节点
        log.info("激活第一个节点");
        if (startNode == null) {
            throw new PlmBizException("流程模板没有开始节点");
        }
        if (CollectionUtils.isNotEmpty(nextAutoNodes)) {
            for (ApmFlowInstanceNode nextAutoNode : nextAutoNodes) {
                activeNode(nextAutoNode, true);
            }
        }
        // 记录历程
        saveFlowProcess(null, instanceBid, "start", null);

        return Boolean.TRUE;
    }

    @NotNull
    private static List<ApmFlowInstanceRoleUser> generateFlowInstanceRoleUsers(String instanceBid, ApmFlowQo qo) {
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = Lists.newArrayList();
        List<ApmRoleUserAO> roleUserList = qo.getRoleUserList();
        if (CollectionUtils.isNotEmpty(roleUserList)) {
            roleUserList.forEach(apmRoleUserAO -> {
                if (CollectionUtils.isEmpty(apmRoleUserAO.getUserList())) {
                    return;
                }
                apmRoleUserAO.getUserList().forEach(user -> {
                    ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                    apmFlowInstanceRoleUser.setSpaceBid(qo.getSpaceBid());
                    apmFlowInstanceRoleUser.setSpaceAppBid(qo.getSpaceAppBid());
                    apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
                    apmFlowInstanceRoleUser.setRoleBid(apmRoleUserAO.getRoleBid());
                    apmFlowInstanceRoleUser.setUserNo(user.getEmpNo());
                    apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
                });
            });
        }
        return apmFlowInstanceRoleUsers;
    }

    @NotNull
    private static List<ApmFlowInstanceNode> generateInstanceNodesFromTemplate(String instanceBid, List<ApmFlowTemplateNodeVO> apmFlowTemplateNodes) {
        Map<String, String> nodeBidMap = Maps.newHashMap();
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = Lists.newArrayList();
        apmFlowTemplateNodes.forEach(apmFlowTemplateNode -> {
            ApmFlowInstanceNode apmFlowInstanceNode = ApmFlowInstanceNodeConverter.INSTANCE.template2Entity(apmFlowTemplateNode);
            String bid = SnowflakeIdWorker.nextIdStr();
            apmFlowInstanceNode.setId(null);
            apmFlowInstanceNode.setBid(bid);
            apmFlowInstanceNode.setInstanceBid(instanceBid);
            apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.NOT_START);
            apmFlowInstanceNode.setNotifyFlag(apmFlowTemplateNode.getNotifyFlag());
            apmFlowInstanceNode.setCreatedTime(new Date());
            apmFlowInstanceNode.setUpdatedTime(new Date());
            apmFlowInstanceNode.setCreatedBy(SsoHelper.getJobNumber());
            apmFlowInstanceNode.setUpdatedBy(SsoHelper.getJobNumber());
            nodeBidMap.put(apmFlowTemplateNode.getBid(), bid);
            apmFlowInstanceNodes.add(apmFlowInstanceNode);
        });
        //替换apmFlowInstanceNodes中的beforeNodeBids
        apmFlowInstanceNodes.forEach(apmFlowInstanceNode -> {
            List<String> beforeNodeBids = apmFlowInstanceNode.getBeforeNodeBids();
            List<String> newBeforeNodeBids = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(beforeNodeBids)) {
                beforeNodeBids.forEach(beforeNodeBid -> newBeforeNodeBids.add(nodeBidMap.get(beforeNodeBid)));
            }
            apmFlowInstanceNode.setBeforeNodeBids(newBeforeNodeBids);
        });
        return apmFlowInstanceNodes;
    }

    @Override
    public ApmFlowInstanceVO listInstanceNodes(String instanceBid) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return null;
        }
        ApmFlowTemplate flowTemplate = apmFlowTemplateService.getByBid(apmFlowInstanceNodes.get(0).getFlowTemplateBid());
        if (flowTemplate == null || FlowTypeEnum.STATE.getCode().equals(flowTemplate.getType())) {
            return null;
        }
        //查询当前用户节点状态,这里如果有两个角色人员出现就只会显示第一个，处理效果一样
        apmFlowInstanceNodes.stream()
                .forEach(apmFlowInstance -> {
                    List<ApmTask> userNodeTaskState = apmTaskApplicationService.getUserNodeTaskState(apmFlowInstance.getBid(), SsoHelper.getJobNumber());
                    if (CollectionUtils.isNotEmpty(userNodeTaskState)) {
                        apmFlowInstance.setTaskState(userNodeTaskState.get(0).getTaskState());
                    }
                });

        //过滤节点重复数据
        Map<String, String> apmFlowWebBidMap = Maps.newHashMap();
        for (int i = apmFlowInstanceNodes.size() - 1; i >= 0; i--) {
            if (apmFlowWebBidMap.containsKey(apmFlowInstanceNodes.get(i).getWebBid())) {
                apmFlowInstanceNodes.remove(i);
            } else {
                apmFlowWebBidMap.put(apmFlowInstanceNodes.get(i).getWebBid(), apmFlowInstanceNodes.get(i).getWebBid());
            }
        }
        //补充流程节点信息
        Set<String> templateNodeBids = apmFlowInstanceNodes.stream().filter(apmFlowInstanceNode -> StringUtils.isNotBlank(apmFlowInstanceNode.getTemplateNodeBid())).map(ApmFlowInstanceNode::getTemplateNodeBid).collect(Collectors.toSet());
        List<ApmFlowTemplateNode> templateNodeVos = apmFlowTemplateNodeService.list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().in(ApmFlowTemplateNode::getBid, templateNodeBids).eq(ApmFlowTemplateNode::getDeleteFlag, false));
        Map<String, ApmFlowTemplateNode> templateNodeVosBidMap = templateNodeVos.stream().collect(Collectors.toMap(ApmFlowTemplateNode::getBid, Function.identity(), (key1, key2) -> key2));
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (StringUtils.isNotBlank(apmFlowInstanceNode.getTemplateNodeBid()) && templateNodeVosBidMap.containsKey(apmFlowInstanceNode.getTemplateNodeBid())) {
                ApmFlowTemplateNode apmFlowTemplateNode = templateNodeVosBidMap.get(apmFlowInstanceNode.getTemplateNodeBid());
                apmFlowInstanceNode.setShowHelpTipFlag(apmFlowTemplateNode.getShowHelpTipFlag());
                apmFlowInstanceNode.setNodeHelpTip(apmFlowTemplateNode.getNodeHelpTip());
            }
        }
        ApmFlowInstanceNode instanceNode = apmFlowInstanceNodes.stream().filter(apmFlowInstanceNode -> FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType()))
                .findFirst().orElseThrow(() -> new PlmBizException("流程实例没有开始节点"));
        return ApmFlowInstanceVO.builder().layout(instanceNode.getLayout()).nodes(apmFlowInstanceNodes).build();
    }

    @Override
    public ApmFlowInstanceVO listInstanceNodesByApmStateQo(ApmStateQo apmStateQo) {
        ApmFlowInstanceVO apmFlowInstanceVO = listInstanceNodes(apmStateQo.getInstanceBid());
        if (apmFlowInstanceVO == null) {
            //查询状态流程
            apmFlowInstanceVO = apmFlowApplicationService.listInstanceNodesByApmStateQo(apmStateQo);
        }
        return apmFlowInstanceVO;
    }

    /**
     * 如果实例是版本对象，替换instanceBid为实例dataBid
     *
     * @param spaceAppBid
     * @param instanceBid
     * @return
     */
    @Override
    public String getVersionInstanceBid(String spaceAppBid, String instanceBid) {
        if (StringUtil.isBlank(spaceAppBid)) {
            return instanceBid;
        }
        //获取空间应用数据
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        if (Objects.isNull(app)) {
            return instanceBid;
        }
        if (Boolean.TRUE.equals(app.getIsVersionObject())) {
            MObject mObject = objectModelCrudI.getHisByBid(app.getModelCode(), instanceBid);
            return mObject.get(VersionObjectEnum.DATA_BID.getCode()).toString();
        }

        return instanceBid;
    }

    /**
     * 获取流程实开始节点的下面节点
     *
     * @param apmFlowInstanceNodes
     * @param startNode
     * @param pageData
     * @return
     */
    private List<ApmFlowInstanceNode> getStartNextNodes(List<ApmFlowInstanceNode> apmFlowInstanceNodes, ApmFlowInstanceNode startNode, MSpaceAppData pageData) {
        List<String> multiPreNodeBids = new ArrayList<>();
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (CollectionUtils.isNotEmpty(apmFlowInstanceNode.getBeforeNodeBids()) && apmFlowInstanceNode.getBeforeNodeBids().size() > 1) {
                multiPreNodeBids.add(apmFlowInstanceNode.getBid());
            }
        }
        List<ApmFlowInstanceNode> nextNodes = getNextNodes(startNode.getBid(), apmFlowInstanceNodes, new ArrayList<>());
        List<ApmFlowInstanceNode> nextActiveNodes = filterCustomerFlowNode(startNode, nextNodes, pageData);
        return nextActiveNodes;
    }

    /**
     * 启动流程 获取事件状态
     *
     * @param instanceBid 实例id
     * @param spaceBid    应用id
     * @param spaceAppBid 应用版本id
     * @param pageData    页面数据
     * @return
     */
    public String runStartNodeGetLifeCycleCode(String instanceBid, String spaceBid, String spaceAppBid, MSpaceAppData pageData) {
        String resLifeCycleCode = null;
        for (int i = 0; i < 3; i++) {
            List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
            String startNodeBid = null;
            if (CollectionUtils.isNotEmpty(apmFlowInstanceNodes)) {
                boolean isStartNode = true;
                ApmFlowInstanceNode startNode = null;
                for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
                    if (!FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType()) && !FlowStateConstant.NOT_START.equals(apmFlowInstanceNode.getNodeState())) {
                        isStartNode = false;
                    }
                    if (FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType())) {
                        startNodeBid = apmFlowInstanceNode.getBid();
                        startNode = apmFlowInstanceNode;
                    }
                }
                if (isStartNode && StringUtils.isNotEmpty(startNodeBid)) {
                    //起始节点需要看提前状态
                    List<ApmFlowInstanceNode> nextActiveNodes = getStartNextNodes(apmFlowInstanceNodes, startNode, pageData);
                    //获取前置改状态的事件
                    if (CollectionUtils.isNotEmpty(nextActiveNodes)) {
                        List<String> nodeBids = nextActiveNodes.stream().map(ApmFlowInstanceNode::getTemplateNodeBid).collect(Collectors.toList());
                        List<ApmFlowNodeEvent> apmFlowNodeEvents = apmFlowNodeEventService.listByNodeBids(nodeBids);
                        if (CollectionUtils.isNotEmpty(apmFlowNodeEvents)) {
                            for (ApmFlowNodeEvent apmFlowNodeEvent : apmFlowNodeEvents) {
                                if (apmFlowNodeEvent.getEventClassification() == 1) {
                                    if (apmFlowNodeEvent.getEventType() == 1) {
                                        resLifeCycleCode = apmFlowNodeEvent.getFiledValue();
                                        break;
                                    }
                                    if (apmFlowNodeEvent.getEventType() == 2 && CommonConst.LIFE_CYCLE_CODE_STR.equals(apmFlowNodeEvent.getFiledName())) {
                                        resLifeCycleCode = apmFlowNodeEvent.getFiledValue();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(resLifeCycleCode)) {
                        //提前更新实例表状态，保证页面及时显示
                        MObject updateMobject = new MObject();
                        updateMobject.put(CommonConst.LIFE_CYCLE_CODE_STR, resLifeCycleCode);
                        pageData.setLifeCycleCode(resLifeCycleCode);
                        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
                        objectModelCrudI.updateByBid(spaceApp.getModelCode(), instanceBid, updateMobject);
                    }
                    completeNode(startNodeBid, new MSpaceAppData());
                    //记录历程
                    saveFlowProcess(startNodeBid, instanceBid, "complete", new HashMap<>(CommonConstant.START_MAP_SIZE));
                }
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }

        }
        return resLifeCycleCode;
    }

    /**
     * 检查流程开始节点没走,如果没走需要重新启动
     *
     * @param instanceBid
     * @return
     */
    @Override
    public void runStartNode(String instanceBid, String spaceBid, String spaceAppBid) {
        for (int i = 0; i < 10; i++) {
            List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
            String startNodeBid = null;
            if (CollectionUtils.isNotEmpty(apmFlowInstanceNodes)) {
                boolean isStartNode = true;
                for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
                    if (!FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType()) && !FlowStateConstant.NOT_START.equals(apmFlowInstanceNode.getNodeState())) {
                        isStartNode = false;
                    }
                    if (FlowNodeTypeConstant.START_NODE.equals(apmFlowInstanceNode.getNodeType())) {
                        startNodeBid = apmFlowInstanceNode.getBid();
                    }
                }
                if (isStartNode && StringUtils.isNotEmpty(startNodeBid)) {
                    //起始节点需要看提前搞状态

                    completeNode(startNodeBid, new MSpaceAppData());
                    //记录历程
                    saveFlowProcess(startNodeBid, instanceBid, "complete", new HashMap<>(CommonConstant.START_MAP_SIZE));
                }
                break;
            } else {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }

        }
    }

    /**
     * 完成节点前的事件检查
     *
     * @param app         应用
     * @param currentNode 当前节点
     * @param mObject     实例数据
     * @return
     */
    @Override
    public void completeNodeCheck(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject) {
        //检查配置节点是否需要检查
        ApmFlowTemplateNode apmFlowTemplateNode = apmFlowTemplateNodeService.getByBid(currentNode.getTemplateNodeBid());
        if (apmFlowTemplateNode.getCompleteCheckFlag() && StringUtils.isNotEmpty(apmFlowTemplateNode.getCompleteCheckEvent())) {
            IFlowCompleteCheckEvent iFlowCompleteCheckEvent = PlmContextHolder.getBean(apmFlowTemplateNode.getCompleteCheckEvent());
            if (iFlowCompleteCheckEvent != null) {
                iFlowCompleteCheckEvent.check(app, currentNode, mObject);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeNode(String nodeBid, MSpaceAppData pageData){
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = completeNodeReturnNextNode(nodeBid, pageData);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ApmFlowInstanceNode> completeNodeReturnNextNode(String nodeBid, MSpaceAppData pageData) {
        //获取节点信息
        ApmFlowInstanceNode currentFlowInstanceNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        if (currentFlowInstanceNode == null) {
            throw new PlmBizException("节点不存在");
        }
        boolean isAutoNode = FlowNodeCompleteType.AUTO_COMPLETE.equals(currentFlowInstanceNode.getComplateType());
        String jobNumber = SsoHelper.getJobNumber();
        long count = 0;
        if (pageData == null || !pageData.containsKey("specialCount")) {
            count = apmTaskApplicationService.checkNeedDealTask(nodeBid, jobNumber);
        } else {
            count = Long.parseLong(pageData.get("specialCount").toString());
        }
       /* if (!(isAutoNode || count > 0)) {
            throw new PlmBizException("当前用户没有待办任务");
        }*/
        if (!(isAutoNode || FlowNodeStateConstant.ACTIVE.equals(currentFlowInstanceNode.getNodeState()))) {
            throw new PlmBizException("流程已审批，请刷新！");
        }
        if (!isAutoNode) {
            List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBid(currentFlowInstanceNode.getInstanceBid());
            if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers) && CollectionUtils.isNotEmpty(currentFlowInstanceNode.getNodeRoleBids())){
                List<String> matchingUserIds = apmFlowInstanceRoleUsers.stream()
                        .filter(user -> currentFlowInstanceNode.getNodeRoleBids().contains(user.getRoleBid()))
                        .map(ApmFlowInstanceRoleUser::getUserNo)
                        .distinct()
                        .collect(Collectors.toList());
                //节点角色是否包含当前用户
                if (matchingUserIds.contains(SsoHelper.getJobNumber()) ){
                    apmTaskApplicationService.completeTask(nodeBid, SsoHelper.getJobNumber());
                }
            }
        }
        //如果是单人完成节点则更改节点状态为已完成
        log.info("完成当前节点:{}", currentFlowInstanceNode.getBid());

        // 完成待办中心任务
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid(), false);
        String content = String.format("您有一个新的任务需要前往【%s】工作台处理", todoCenterAppName);
        String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mSpaceAppData.getSpaceBid(), currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid());
        CompletableFuture.runAsync(() -> toDoCenterService.pushTodoTaskState(currentFlowInstanceNode, mSpaceAppData, Lists.newArrayList(jobNumber), content, url, TodoCenterConstant.AGREE, TodoCenterConstant.APPROVAL_COMPLETE), SimpleThreadPool.getInstance());
//        if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType()) && !apmTaskApplicationService.checkCompleteTask(nodeBid, SsoHelper.getJobNumber())) {
//            log.info("当前节点还有待办任务未完成:{}", currentFlowInstanceNode.getBid());
//            return Boolean.TRUE;
//        }
        if (handlerAuthNode(currentFlowInstanceNode, nodeBid)) {
            return null;
        }
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = deleteUserTaskAndCompleteNodeByPageData(currentFlowInstanceNode, pageData);
        return apmFlowInstanceNodes;
    }

    /**
     * 处理授权角色
     * @param currentFlowInstanceNode
     * @param nodeBid
     * @return
     */
    private Boolean handlerAuthNode(ApmFlowInstanceNode currentFlowInstanceNode,String nodeBid){
        // 查询流程对应角色用户信息
        if (!FlowNodeCompleteType.AUTO_COMPLETE.equals(currentFlowInstanceNode.getComplateType())) {
            //节点角色，和 审核节点角色如果有一个为空 按照原逻辑走
            if(CollectionUtils.isNotEmpty(currentFlowInstanceNode.getNodeRoleBids()) && CollectionUtils.isEmpty(currentFlowInstanceNode.getComplateRoleBids())){
                if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType()) && !apmTaskApplicationService.checkCompleteTask(nodeBid, SsoHelper.getJobNumber())) {
                    log.info("当前节点还有待办任务未完成:{}", currentFlowInstanceNode.getBid());
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if(CollectionUtils.isEmpty(currentFlowInstanceNode.getNodeRoleBids()) && CollectionUtils.isNotEmpty(currentFlowInstanceNode.getComplateRoleBids())){
                apmTaskApplicationService.completeTask(nodeBid, SsoHelper.getJobNumber());
                if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType()) && !apmTaskApplicationService.checkCompleteTask(nodeBid, SsoHelper.getJobNumber())) {
                    log.info("当前节点还有待办任务未完成:{}", currentFlowInstanceNode.getBid());
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if (CollectionUtils.isEmpty(currentFlowInstanceNode.getComplateRoleBids())) {
                return Boolean.FALSE;
            }
            List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBid(currentFlowInstanceNode.getInstanceBid());
            //审核节点角色如果没有一个人 也需要拦截
            Map<String,String> completeRoleMap = currentFlowInstanceNode.getComplateRoleBids().stream().collect(Collectors.toMap(Function.identity(),Function.identity()));
            int completeRoleUesrNum = 0;
            for(ApmFlowInstanceRoleUser apmFlowInstanceRoleUser:apmFlowInstanceRoleUsers){
                if(completeRoleMap.containsKey(apmFlowInstanceRoleUser.getRoleBid())){
                    completeRoleUesrNum ++;
                }
            }
            if(completeRoleUesrNum == 0){
                return Boolean.TRUE;
            }
            List<String> nodeBids =null;
            if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                nodeBids = apmFlowInstanceRoleUsers.stream()
                        .filter(user -> currentFlowInstanceNode.getNodeRoleBids().contains(user.getRoleBid()))
                        .map(ApmFlowInstanceRoleUser::getUserNo)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)) {
                List<String> matchingUserIds = getMatchingUserIds(apmFlowInstanceRoleUsers, currentFlowInstanceNode.getComplateRoleBids());
                //所有都是节点角色，么有授权节点也是处理成完成
                if (CollectionUtils.isEmpty(matchingUserIds)){
                    if (CollectionUtils.isNotEmpty(nodeBids) && nodeBids.contains(SsoHelper.getJobNumber())){
                       if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType()) && !apmTaskApplicationService.checkCompleteTask(nodeBid, SsoHelper.getJobNumber())) {
                           log.info("当前节点还有待办任务未完成:{}", currentFlowInstanceNode.getBid());
                           return Boolean.TRUE;
                       }
                        return Boolean.FALSE;
                    }
                }
                if (matchingUserIds != null && !matchingUserIds.contains(SsoHelper.getJobNumber())) {
                    return Boolean.TRUE;
                }
                if (FlowNodeCompleteType.SINGLE_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType())) {
                    log.info("当前节点为单人确认节点，直接激活后续节点");
                    if (matchingUserIds != null && !matchingUserIds.contains(SsoHelper.getJobNumber())) {
                        log.info("当前角色非授权角色，无需流转下个节点");
                        return Boolean.TRUE;
                    }
                    apmTaskApplicationService.overAllTask(nodeBid);
                } else if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentFlowInstanceNode.getComplateType())) {
                    //如果授权角色和节点角色为自己，直接处理为完成
                    List<String> nodeUserNos = nodeBids.stream().filter(user -> user.equals(SsoHelper.getJobNumber())).distinct().collect(Collectors.toList());
                    List<String> authUserNos = matchingUserIds.stream().filter(user -> user.equals(SsoHelper.getJobNumber())).distinct().collect(Collectors.toList());
                    if ( (nodeUserNos.size() == 1 && nodeUserNos.size() == nodeBids.size())&& (authUserNos.size() == 1 && authUserNos.size() == matchingUserIds.size())){
                        return Boolean.FALSE;
                    }
                    //如果角色中没有人则报错
                    if (CollectionUtils.isEmpty(matchingUserIds)){
                        throw new PlmBizException("当前有角色未配置人员,请检查配置或添加人员");
                    }
                    log.info("当前节点为多人确认节点，判断后续节点是否需要激活");
                    completeTasksForCurrentUser(nodeBid);
                    List<String> authUserEmpNos = matchingUserIds.stream().filter(user -> !user.equals(SsoHelper.getJobNumber())).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(authUserEmpNos)){
                        return Boolean.FALSE;
                    }
                    List<ApmTask> apmTasks = apmTaskApplicationService.checkCompleteRoleTask(nodeBid, authUserEmpNos);
                    if (CollectionUtils.isNotEmpty(apmTasks)) {
                        log.info("当前节点授权角色未完成，先处理待办后，在处理流程节点");
                        return Boolean.TRUE;
                    }
                    apmTaskApplicationService.overAllTask(nodeBid);
                }
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 获取授权角色
     * @param apmFlowInstanceRoleUsers
     * @param roleBids
     * @return
     */
    private List<String> getMatchingUserIds(List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers, List<String> roleBids) {
        if (CollectionUtils.isEmpty(apmFlowInstanceRoleUsers) || CollectionUtils.isEmpty(roleBids)){
           return null;
        }
        return apmFlowInstanceRoleUsers.stream()
                .filter(user -> roleBids.contains(user.getRoleBid()))
                .map(ApmFlowInstanceRoleUser::getUserNo)
                .collect(Collectors.toList());
    }

    /**
     * 更新当前用户任务状态为已完成
     * @param nodeBid
     */
    private void completeTasksForCurrentUser(String nodeBid) {
        List<ApmTask> apmTasks = apmTaskMapper.selectList(
                new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, nodeBid)
                        .eq(ApmTask::getTaskHandler, SsoHelper.getJobNumber()));
        if (CollectionUtils.isNotEmpty(apmTasks)) {
            apmTasks.forEach(task -> {
                task.setTaskState(FlowNodeStateConstant.COMPLETED);
                apmTaskMapper.updateById(task);
            });
        }
    }

    @Override
    @Transactional
    public Boolean completeNodeForce(String nodeBid) {
        log.info("强制完成节点:{}", nodeBid);
        //获取节点信息
        ApmFlowInstanceNode currentFlowInstanceNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        if (currentFlowInstanceNode == null) {
            log.error("节点不存在,bid={}", nodeBid);
            return Boolean.FALSE;
        }
        if (!FlowNodeStateConstant.ACTIVE.equals(currentFlowInstanceNode.getNodeState())) {
            log.error("节点不是激活状态,bid={}", nodeBid);
            return Boolean.FALSE;
        }
        //如果是单人完成节点则更改节点状态为已完成
        log.info("完成当前节点:{}", currentFlowInstanceNode.getBid());
        Boolean result = deleteUserTaskAndCompleteNode(currentFlowInstanceNode);
        // 完成待办中心任务
        if (Boolean.TRUE.equals(result)) {
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid(), false);
            String content = String.format("您有一个新的任务需要前往【%s】工作台处理", todoCenterAppName);
            String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mSpaceAppData.getSpaceBid(), currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid());
            CompletableFuture.runAsync(() -> toDoCenterService.pushTodoTaskState(currentFlowInstanceNode, mSpaceAppData, Lists.newArrayList(SsoHelper.getJobNumber()), content, url, TodoCenterConstant.AGREE, TodoCenterConstant.APPROVAL_COMPLETE), SimpleThreadPool.getInstance());
        }
        return result;
    }

    @NotNull
    @Subscribe
    private Boolean deleteUserTaskAndCompleteNode(ApmFlowInstanceNode currentFlowInstanceNode) {
        apmTaskApplicationService.deleteNotComplete(currentFlowInstanceNode.getBid(), null, TaskTypeConstant.FLOW);
        completeNode(currentFlowInstanceNode,null);
        log.info("激活后续节点,{}", currentFlowInstanceNode.getBid());
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(currentFlowInstanceNode.getInstanceBid());
        //梳理出有多个前置节点的节点数据
        List<String> multiPreNodeBids = new ArrayList<>();
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (CollectionUtils.isNotEmpty(apmFlowInstanceNode.getBeforeNodeBids()) && apmFlowInstanceNode.getBeforeNodeBids().size() > 1) {
                multiPreNodeBids.add(apmFlowInstanceNode.getBid());
            }
        }
        Map<String, ApmFlowInstanceNode> instanceNodeMap = apmFlowInstanceNodes.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getBid, Function.identity()));
        //查看当前节点的后续节点
        List<ApmFlowInstanceNode> nextNodes = getNextNodes(currentFlowInstanceNode.getBid(), apmFlowInstanceNodes, new ArrayList<>());
        //需求管理流程特殊处理，根据条件判断是否激活后续节点
        log.info("过滤前的节点:{}", JSON.toJSONString(nextNodes));
        List<ApmFlowInstanceNode> nextActiveNodes = filterCustomerFlowNode(currentFlowInstanceNode, nextNodes, null);
        log.info("过滤后的节点:{}", JSON.toJSONString(nextActiveNodes));
        //如果后续节点不为空，且后续节点的所有前置节点都已完成，则激活后续节点
        activeNextNodes(currentFlowInstanceNode, instanceNodeMap, nextActiveNodes, multiPreNodeBids);
        log.info("更新后续节点状态");
        apmFlowInstanceNodeService.updateBatchById(nextActiveNodes);
        return Boolean.TRUE;
    }

    private List<ApmFlowInstanceNode> deleteUserTaskAndCompleteNodeByPageData(ApmFlowInstanceNode currentFlowInstanceNode, MSpaceAppData pageData) {
        apmTaskApplicationService.deleteNotComplete(currentFlowInstanceNode.getBid(), SsoHelper.getJobNumber(), TaskTypeConstant.FLOW);
        completeNode(currentFlowInstanceNode,pageData);
        log.info("激活后续节点,{}", currentFlowInstanceNode.getBid());
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(currentFlowInstanceNode.getInstanceBid());
        //梳理出有多个前置节点的节点数据
        List<String> multiPreNodeBids = new ArrayList<>();
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (CollectionUtils.isNotEmpty(apmFlowInstanceNode.getBeforeNodeBids()) && apmFlowInstanceNode.getBeforeNodeBids().size() > 1) {
                multiPreNodeBids.add(apmFlowInstanceNode.getBid());
            }
        }
        Map<String, ApmFlowInstanceNode> instanceNodeMap = apmFlowInstanceNodes.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getBid, Function.identity()));
        //查看当前节点的后续节点
        List<ApmFlowInstanceNode> nextNodes = getNextNodes(currentFlowInstanceNode.getBid(), apmFlowInstanceNodes, new ArrayList<>());
        //需求管理流程特殊处理，根据条件判断是否激活后续节点
        log.info("过滤前的节点:{}", JSON.toJSONString(nextNodes));
        List<ApmFlowInstanceNode> nextActiveNodes = filterCustomerFlowNode(currentFlowInstanceNode, nextNodes, pageData);
        log.info("过滤后的节点:{}", JSON.toJSONString(nextActiveNodes));
        //判断后续节点是否有完成节点，如果有完成节点 需要激活完成节点 且把其他进行中节点变成未开始，同时删除对应的任务
        boolean hasCompleteNode = false;
        if (CollectionUtils.isNotEmpty(nextActiveNodes)) {
            for (ApmFlowInstanceNode nextNode : nextActiveNodes) {
                if (nextNode.getNodeType() != null && nextNode.getNodeType() == 2) {
                    //如果是完成节点
                    completeNode(nextNode,pageData);
                    //把其他进行中节点变成未开始
                    List<String> notInBids = new ArrayList<>();
                    notInBids.add(nextNode.getBid());
                    notInBids.add(currentFlowInstanceNode.getBid());
                    List<ApmFlowInstanceNode> apmActiveFlowInstanceNodes = apmFlowInstanceNodeService.listActiveByInstanceBid(currentFlowInstanceNode.getInstanceBid(), notInBids);
                    if (CollectionUtils.isNotEmpty(apmActiveFlowInstanceNodes)) {
                        List<String> apmActiveFlowInstanceNodeBids = apmActiveFlowInstanceNodes.stream().map(ApmFlowInstanceNode::getBid).collect(Collectors.toList());
                        //删除任务
                        apmTaskApplicationService.deleteByBizBids(TaskTypeConstant.FLOW, apmActiveFlowInstanceNodeBids);
                        for (ApmFlowInstanceNode apmActiveFlowInstanceNode : apmActiveFlowInstanceNodes) {
                            apmActiveFlowInstanceNode.setNodeState(FlowNodeStateConstant.NOT_START);
                        }
                        //更新状态
                        apmFlowInstanceNodeService.updateBatchById(apmActiveFlowInstanceNodes);
                    }
                    hasCompleteNode = true;
                }
            }
        }
        //如果后续节点不为空，且后续节点的所有前置节点都已完成，则激活后续节点
        if (!hasCompleteNode) {
            activeNextNodes(currentFlowInstanceNode, instanceNodeMap, nextActiveNodes, multiPreNodeBids);
            log.info("更新后续节点状态");
            apmFlowInstanceNodeService.updateBatchById(nextActiveNodes);
        }
        return nextActiveNodes;
    }

    private Date getObjectDate(Object object) {
        if (object instanceof Date) {
            return (Date) object;
        } else if (object instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) object;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else {
            if (object.toString().length() == DatePattern.NORM_DATETIME_PATTERN.length()) {
                return DateUtil.parseDateTime(object.toString());
            } else if (object.toString().length() == DatePattern.NORM_DATE_PATTERN.length()) {
                return DateUtil.parseDate(object.toString());
            }
        }
        return null;
    }


    /**
     * @param apmFlowNodeDirectionVO
     * @param apmFlowNodeDirectionConditionList
     * @param mSpaceAppData
     * @return boolean
     */
    private boolean checkNodeDirectionCondition(ApmFlowNodeDirectionVO apmFlowNodeDirectionVO, List<ApmFlowNodeDirectionCondition> apmFlowNodeDirectionConditionList, MSpaceAppData mSpaceAppData) {
        if (CollectionUtils.isEmpty(apmFlowNodeDirectionConditionList)) {
            return true;
        }
        for (ApmFlowNodeDirectionCondition apmFlowNodeDirectionCondition : apmFlowNodeDirectionConditionList) {
            /**
             * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于，between.在区间
             */
            String relationship = apmFlowNodeDirectionCondition.getRelationship();
            String filedName = apmFlowNodeDirectionCondition.getFiledName();
            String filedValue = apmFlowNodeDirectionCondition.getFiledValue();
            /**
             * 条件比较值类型，string.字符串，number.数字，date.日期，role.角色，now.日期当前时间。loginUser.当前登录人工号
             */
            String filedValueType = apmFlowNodeDirectionCondition.getFiledValueType();

            Object value = mSpaceAppData.get(filedName);
            boolean result = false;
            //空判断
            if ("null".equals(relationship)) {
                if (value == null) {
                    result = true;
                } else {
                    List<String> valueList = getObjectList(value);
                    if (CollectionUtils.isEmpty(valueList)) {
                        result = true;
                    }
                }
            }
            //非空判断
            if ("notNull".equals(relationship)) {
                if (value != null) {
                    List<String> valueList = getObjectList(value);
                    if (CollectionUtils.isNotEmpty(valueList)) {
                        result = true;
                    }
                }
            }
            //包含
            if ("contain".equals(relationship)) {
                if (value != null && value.toString().contains(filedValue)) {
                    result = true;
                }
            }
            //不包含
            if ("notContain".equals(relationship)) {
                if (value != null && !value.toString().contains(filedValue)) {
                    result = true;
                }
            }
            //等于
            if ("equal".equals(relationship)) {
                if (value != null && value.toString().equals(filedValue)) {
                    result = true;
                }
            }
            //不等于
            if ("notEqual".equals(relationship)) {
                if (value != null && !value.toString().equals(filedValue)) {
                    result = true;
                }
            }
            //gt.大于，
            if ("gt".equals(relationship) && value != null) {
                if ("number".equals(filedValueType)) {
                    //数字类型判断
                    Double valueDouble = Double.valueOf(value.toString());
                    Double filedValueDouble = Double.valueOf(filedValue);
                    if (valueDouble > filedValueDouble) {
                        result = true;
                    }
                } else if ("date".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = DateUtil.parseDateTime(filedValue);
                    if (valueDate.getTime() > filedValueDate.getTime()) {
                        result = true;
                    }
                } else if ("now".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = new Date();
                    if (valueDate.getTime() > filedValueDate.getTime()) {
                        result = true;
                    }
                } else {
                    if (value != null && value.toString().compareTo(filedValue) > 0) {
                        result = true;
                    }
                }
            }
            //lt.小于，
            if ("lt".equals(relationship) && value != null) {
                if ("number".equals(filedValueType)) {
                    //数字类型判断
                    Double valueDouble = Double.valueOf(value.toString());
                    Double filedValueDouble = Double.valueOf(filedValue);
                    if (valueDouble < filedValueDouble) {
                        result = true;
                    }
                } else if ("date".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = DateUtil.parseDateTime(filedValue);
                    if (valueDate.getTime() < filedValueDate.getTime()) {
                        result = true;
                    }
                } else if ("now".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = new Date();
                    if (valueDate.getTime() < filedValueDate.getTime()) {
                        result = true;
                    }
                } else {
                    if (value != null && value.toString().compareTo(filedValue) < 0) {
                        result = true;
                    }
                }
            }
            //notGt.小于等于，
            if ("notGt".equals(relationship) && value != null) {
                if ("number".equals(filedValueType)) {
                    //数字类型判断
                    Double valueDouble = Double.valueOf(value.toString());
                    Double filedValueDouble = Double.valueOf(filedValue);
                    if (valueDouble <= filedValueDouble) {
                        result = true;
                    }
                } else if ("date".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = DateUtil.parseDateTime(filedValue);
                    if (valueDate.getTime() <= filedValueDate.getTime()) {
                        result = true;
                    }
                } else if ("now".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = new Date();
                    if (valueDate.getTime() <= filedValueDate.getTime()) {
                        result = true;
                    }
                } else {
                    if (value != null && value.toString().compareTo(filedValue) <= 0) {
                        result = true;
                    }
                }
            }
            //notLt.大于等于
            if ("notLt".equals(relationship)) {
                if ("number".equals(filedValueType)) {
                    //数字类型判断
                    Double valueDouble = Double.valueOf(value.toString());
                    Double filedValueDouble = Double.valueOf(filedValue);
                    if (valueDouble >= filedValueDouble) {
                        result = true;
                    }
                } else if ("date".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = DateUtil.parseDateTime(filedValue);
                    if (valueDate.getTime() >= filedValueDate.getTime()) {
                        result = true;
                    }
                } else if ("now".equals(filedValueType)) {
                    //日期类型
                    Date valueDate = getObjectDate(value);
                    Date filedValueDate = new Date();
                    if (valueDate.getTime() >= filedValueDate.getTime()) {
                        result = true;
                    }
                } else {
                    if (value != null && value.toString().compareTo(filedValue) >= 0) {
                        result = true;
                    }
                }
            }
            if (FlowEnum.FLOW_MATCH_ALL.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch()) && !result) {
                return false;
            }
            if (FlowEnum.FLOW_MATCH_ANY.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch()) && result) {
                return true;
            }
        }
        if (FlowEnum.FLOW_MATCH_ALL.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch())) {
            return true;
        }
        if (FlowEnum.FLOW_MATCH_ANY.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch())) {
            return false;
        }
        return false;
    }

    private List<ApmFlowInstanceNode> geDirectionConditionNextNodes(ApmFlowInstanceNode currentFlowInstanceNode, List<ApmFlowInstanceNode> nextNodes, MSpaceAppData pageData) {
        //查询当前节点到下个节点的路由配置新增
        List<ApmFlowNodeDirectionVO> apmFlowNodeDirectionVOS = apmFlowApplicationService.listDirectionVOByNodeBid(currentFlowInstanceNode.getTemplateNodeBid());
        if (CollectionUtils.isEmpty(apmFlowNodeDirectionVOS)) {
            return nextNodes;
        }
        Map<String, ApmFlowNodeDirectionVO> apmFlowNodeDirectionVOMap = apmFlowNodeDirectionVOS.stream().collect(Collectors.toMap(ApmFlowNodeDirectionVO::getTargetNodeBid, Function.identity()));
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid(), false);
        if (mSpaceAppData == null || mSpaceAppData.size() == 0) {
            mSpaceAppData = new MSpaceAppData();
        }
        if (pageData != null) {
            mSpaceAppData.putAll(pageData);
        }
        for (int i = nextNodes.size() - 1; i >= 0; i--) {
            ApmFlowInstanceNode apmFlowInstanceNode = nextNodes.get(i);
            ApmFlowNodeDirectionVO apmFlowNodeDirectionVO = apmFlowNodeDirectionVOMap.get(apmFlowInstanceNode.getTemplateNodeBid());
            if (apmFlowNodeDirectionVO != null && CollectionUtils.isNotEmpty(apmFlowNodeDirectionVO.getApmFlowNodeDirectionConditionList())) {
                // 这里设置线名称！方便后续历程可以获取
                apmFlowInstanceNode.setLineName(apmFlowNodeDirectionVO.getLineName());
                boolean conditionCheck = checkNodeDirectionCondition(apmFlowNodeDirectionVO, apmFlowNodeDirectionVO.getApmFlowNodeDirectionConditionList(), mSpaceAppData);
                if (!conditionCheck) {
                    nextNodes.remove(i);
                }
            }
        }
        if (CollectionUtils.isEmpty(nextNodes)) {
            String jsonStr = JSON.toJSONString(mSpaceAppData);
            log.error("流程节点流转条件不满足！" + jsonStr);
        }
        return nextNodes;
    }

    private List<ApmFlowInstanceNode> filterCustomerFlowNode(ApmFlowInstanceNode currentFlowInstanceNode, List<ApmFlowInstanceNode> nextNodes, MSpaceAppData pageData) {

        return geDirectionConditionNextNodes(currentFlowInstanceNode, nextNodes, pageData);
        /*if (!customerFlowBid.equals(currentFlowInstanceNode.getFlowTemplateBid())) {
            geDirectionConditionNextNodes(currentFlowInstanceNode, nextNodes, pageData);
            return nextNodes;
        }
        if("是否都通过判断节点".equals(currentFlowInstanceNode.getNodeName())){
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid());
            String isRatReview = (String)mSpaceAppData.get("isRatReview");
            String isSETechEvaluationPassed = (String)mSpaceAppData.get("isSETechEvaluationPassed");
            if(pass.equals(isRatReview) && pass.equals(isSETechEvaluationPassed)){
                for (ApmFlowInstanceNode nextNode : nextNodes) {
                    if (nextNode.getNodeName().equals("领域负责人评估")) {
                        return Lists.newArrayList(nextNode);
                    }
                }
            } else {
                for (ApmFlowInstanceNode nextNode : nextNodes) {
                    if (!nextNode.getNodeName().equals("领域负责人评估")) {
                        return Lists.newArrayList(nextNode);
                    }
                }
            }
        }else if("领域负责人评估".equals(currentFlowInstanceNode.getNodeName())){
            return filterByFieldValue(currentFlowInstanceNode, nextNodes, "isDomainAssessmentPassed", "领域负责人分发");
        } else if ("SRE过滤".equals(currentFlowInstanceNode.getNodeName())) {
            return filterByFieldValue(currentFlowInstanceNode, nextNodes, "sreglpgyyjcn", "提出人确认结果");
        } else if ("提出人确认结果".equals(currentFlowInstanceNode.getNodeName())) {
            return filterByFieldValue(currentFlowInstanceNode, nextNodes, "tcrqrjgsfjs", "完成节点");
        } else if ("RMT评估".equals(currentFlowInstanceNode.getNodeName())) {
            return filterByFieldValue(currentFlowInstanceNode, nextNodes, "rmtpgsfcn", "领域负责人分发");
        }
        log.info("过滤后的下一节点为：{}", nextNodes);
        return nextNodes;*/
    }

    @Nullable
    private List<ApmFlowInstanceNode> filterByFieldValue(ApmFlowInstanceNode currentFlowInstanceNode, List<ApmFlowInstanceNode> nextNodes, String fieldName, String nodeNameY) {
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(currentFlowInstanceNode.getSpaceAppBid(), currentFlowInstanceNode.getInstanceBid(), false);
        String isRatReview = (String) mSpaceAppData.get(fieldName);
        if (pass.equals(isRatReview)) {
            for (ApmFlowInstanceNode nextNode : nextNodes) {
                if (nextNode.getNodeName().equals(nodeNameY)) {
                    return Lists.newArrayList(nextNode);
                }
            }
        } else {
            for (ApmFlowInstanceNode nextNode : nextNodes) {
                if (!nextNode.getNodeName().equals(nodeNameY)) {
                    return Lists.newArrayList(nextNode);
                }
            }
        }
        return nextNodes;
    }

    /**
     * 获取指定节点所有前置完成节点
     *
     * @param thisNode         当前节点
     * @param instanceNodeMap  所有节点
     * @param allCompleteNodes 所有前置完成节点
     * @param haveHandelNodes  已处理节点
     */
    private void getAllBeforeCompleteNode(ApmFlowInstanceNode thisNode, Map<String, ApmFlowInstanceNode> instanceNodeMap, List<String> allCompleteNodes, List<String> haveHandelNodes) {
        if (thisNode != null) {
            allCompleteNodes.add(thisNode.getBid());
            List<String> beforeNodeBids = thisNode.getBeforeNodeBids();
            if (CollectionUtils.isNotEmpty(beforeNodeBids)) {
                for (String beforeNodeBid : beforeNodeBids) {
                    if (!haveHandelNodes.contains(beforeNodeBid)) {
                        haveHandelNodes.add(beforeNodeBid);
                        ApmFlowInstanceNode beforeNode = instanceNodeMap.get(beforeNodeBid);
                        if (beforeNode != null && FlowNodeStateConstant.COMPLETED.equals(beforeNode.getNodeState())) {
                            //节点是完成节点，获取前置所有完成节点
                            getAllBeforeCompleteNode(beforeNode, instanceNodeMap, allCompleteNodes, haveHandelNodes);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取指定节点所有前置进行中节点
     *
     * @param beforeNodeBids  前置节点
     * @param instanceNodeMap 所有节点
     * @param allActiveNodes  所有前置进行中节点
     */
    private void getFirstActiveNode(List<String> beforeNodeBids, Map<String, ApmFlowInstanceNode> instanceNodeMap, List<String> allActiveNodes, List<String> haveHandleNodes) {
        if (CollectionUtils.isNotEmpty(beforeNodeBids) && CollectionUtils.isEmpty(allActiveNodes)) {
            for (String beforeNodeBid : beforeNodeBids) {
                if (!haveHandleNodes.contains(beforeNodeBid)) {
                    haveHandleNodes.add(beforeNodeBid);
                    ApmFlowInstanceNode beforeNode = instanceNodeMap.get(beforeNodeBid);
                    if (FlowNodeStateConstant.ACTIVE.equals(beforeNode.getNodeState())) {
                        allActiveNodes.add(beforeNodeBid);
                        break;
                    } else if (FlowNodeStateConstant.NOT_START.equals(beforeNode.getNodeState())) {
                        List<String> thisBeforeNodeBids = beforeNode.getBeforeNodeBids();
                        getFirstActiveNode(thisBeforeNodeBids, instanceNodeMap, allActiveNodes, haveHandleNodes);
                    }
                }
            }

        }
    }

    /**
     * 该条线路是否激活
     *
     * @param allBeforeCompleteNodeBids 前置所有完成节点
     * @param allActiveNodes            所有前置进行中节点
     * @return
     */
    private boolean checkFlowCompleteNodes(List<String> allBeforeCompleteNodeBids, List<String> allActiveNodes) {
        if (CollectionUtils.isEmpty(allActiveNodes)) {
            return false;
        }
        for (String activeNodeBid : allActiveNodes) {
            if (!allBeforeCompleteNodeBids.contains(activeNodeBid)) {
                return true;
            }
        }
        return false;
    }

    private void activeNextNodes(ApmFlowInstanceNode currentFlowInstanceNode, Map<String, ApmFlowInstanceNode> instanceNodeMap, List<ApmFlowInstanceNode> nextNodes, List<String> multiPreNodeBids) {
        for (ApmFlowInstanceNode nextNode : nextNodes) {
            if (FlowNodeTypeConstant.START_NODE.equals(currentFlowInstanceNode.getNodeType())) {
                //开始节点直接激活下个节点
                activeNode(nextNode, true);
                continue;
            }
            List<String> beforeNodeBids = nextNode.getBeforeNodeBids();
            //获取当前需要机会的节点
            List<String> allBeforeCompleteNodeBids = new ArrayList<>();
            if (CommonConst.ALL.equals(nextNode.getActiveMatch()) && CollectionUtils.isNotEmpty(beforeNodeBids)) {
                //判断其他流程是否完成 没激活的流程不算
                for (String beforeNodeBid : beforeNodeBids) {
                    ApmFlowInstanceNode beforeNode = instanceNodeMap.get(beforeNodeBid);
                    if (currentFlowInstanceNode.getBid().equals(beforeNode.getBid()) || FlowNodeStateConstant.COMPLETED.equals(beforeNode.getNodeState())) {
                        //节点是完成节点，获取前置所有完成节点
                        List<String> nodeBeforeAllCompleteNodeBids = new ArrayList<>();
                        List<String> haveHandleNodes = new ArrayList<>();
                        getAllBeforeCompleteNode(beforeNode, instanceNodeMap, nodeBeforeAllCompleteNodeBids, haveHandleNodes);
                        allBeforeCompleteNodeBids.addAll(nodeBeforeAllCompleteNodeBids);
                    }
                }
            }
            boolean allBeforeNodeCompleted = true;
            if (CollectionUtils.isNotEmpty(beforeNodeBids)) {
                for (String beforeNodeBid : beforeNodeBids) {
                    ApmFlowInstanceNode beforeNode = instanceNodeMap.get(beforeNodeBid);
                    if (!currentFlowInstanceNode.getBid().equals(beforeNode.getBid())) {
                        if (FlowNodeStateConstant.ACTIVE.equals(beforeNode.getNodeState())) {
                            //进行中节点 直接中断
                            allBeforeNodeCompleted = false;
                            log.info("前置节点:{}未完成", beforeNode);
                            break;
                        } else if (CommonConst.ALL.equals(nextNode.getActiveMatch()) && FlowNodeStateConstant.NOT_START.equals(beforeNode.getNodeState())) {
                            //没激活节点需要判断当前路线是否激活
                            List<String> allActiveNodes = new ArrayList<>();
                            List<String> haveHandleNodes = new ArrayList<>();
                            getFirstActiveNode(beforeNode.getBeforeNodeBids(), instanceNodeMap, allActiveNodes, haveHandleNodes);
                            boolean check = checkFlowCompleteNodes(allBeforeCompleteNodeBids, allActiveNodes);
                            if (check) {
                                //进行中节点 直接中断
                                allBeforeNodeCompleted = false;
                                log.info("前置节点:{}未完成", beforeNode);
                                break;
                            }
                        }

                    }
                }
            }
            boolean skipWait = false;
            if (multiPreNodeBids.contains(nextNode.getBid()) && CommonConst.ANY.equals(nextNode.getActiveMatch())) {
                //有多个前置节点
                skipWait = true;
            }
            if (allBeforeNodeCompleted || skipWait) {
                log.info("activeNextNodes nextNode:{}", JSON.toJSONString(nextNode));
                activeNode(nextNode, true);
            }
        }
    }

    private List<ApmFlowInstanceNode> getNextNodes(String nodeBid, List<ApmFlowInstanceNode> apmFlowInstanceNodes, List<String> nodeBidList) {
        List<ApmFlowInstanceNode> nextNodes = Lists.newArrayList();
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            if (CollectionUtils.isEmpty(apmFlowInstanceNode.getBeforeNodeBids()) || nodeBidList.contains(apmFlowInstanceNode.getBid())) {
                continue;
            }
            if (apmFlowInstanceNode.getBeforeNodeBids().contains(nodeBid)) {
                nextNodes.add(apmFlowInstanceNode);
                nodeBidList.add(apmFlowInstanceNode.getBid());
            }
        }
        return nextNodes;
    }

    @Override
    public Boolean rollback(String nodeBid, boolean runEvent) {
        ApmFlowInstanceNode flowInstanceNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        if (flowInstanceNode == null) {
            throw new PlmBizException("节点不存在");
        }
        log.info("回退节点:{}", flowInstanceNode.getBid());
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(flowInstanceNode.getInstanceBid())
                .stream().filter(apmFlowInstanceNode -> !FlowNodeStateConstant.NOT_START.equals(apmFlowInstanceNode.getNodeState())).collect(Collectors.toList());
        //查找当前节点的所有后续节点
        List<ApmFlowInstanceNode> subsequentNodes = getSubsequentNodes(nodeBid, apmFlowInstanceNodes, new ArrayList<>());
        subsequentNodes.add(flowInstanceNode);
        subsequentNodes.forEach(
                this::cancelActiveNode
        );
        activeNode(flowInstanceNode, runEvent);
        apmFlowInstanceNodeService.updateBatchById(subsequentNodes);
        return Boolean.TRUE;
    }

    @Override
    public List<ApmRoleUserAO> listNodeRoleUser(String nodeBid) {
        Assert.hasText(nodeBid, "nodeBid不能为空");
        ApmFlowInstanceNode flowInstanceNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        if (flowInstanceNode == null) {
            throw new PlmBizException("节点不存在");
        }
        List<String> combinedList = Stream.concat(
                Optional.ofNullable(flowInstanceNode.getNodeRoleBids()).orElse(Collections.emptyList()).stream(),
                Optional.ofNullable(flowInstanceNode.getComplateRoleBids()).orElse(Collections.emptyList()).stream()
        ).collect(Collectors.toList());
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndRoles(flowInstanceNode.getInstanceBid(),combinedList);
        return buildRoleUserAOList(apmFlowInstanceRoleUsers, combinedList);
    }

    @Override
    public List<ApmRoleUserAO> listLifeCycleCodeRoleUser(String lifeCycleCode, String instanceBid) {
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndLifeCycleCode(instanceBid, lifeCycleCode);
        if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)) {
            List<String> roleBids = apmFlowInstanceRoleUsers.stream().map(ApmFlowInstanceRoleUser::getRoleBid).collect(Collectors.toList());
            return buildRoleUserAOList(apmFlowInstanceRoleUsers, roleBids);
        }
        return Collections.emptyList();
    }

    @NotNull
    private static List<ApmRoleUserAO> buildRoleUserAOList(List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers, List<String> nodeRoleBids) {
        Map<String, List<ApmFlowInstanceRoleUser>> roleUserMap = apmFlowInstanceRoleUsers.stream().collect(Collectors.groupingBy(ApmFlowInstanceRoleUser::getRoleBid));
        List<ApmRoleUserAO> apmRoleUserAOList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(nodeRoleBids)) {
            return apmRoleUserAOList;
        }
        nodeRoleBids.forEach((roleBid) -> {
            ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
            apmRoleUserAO.setRoleBid(roleBid);
            List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUserList = roleUserMap.get(roleBid);
            if (CollectionUtils.isEmpty(apmFlowInstanceRoleUserList)) {
                apmRoleUserAOList.add(apmRoleUserAO);
                return;
            }
            List<ApmUser> userList = new ArrayList<>();
            for (ApmFlowInstanceRoleUser apmFlowInstanceRoleUser : apmFlowInstanceRoleUserList) {
                if (StringUtils.isNotEmpty(apmFlowInstanceRoleUser.getUserNo())) {
                    ApmUser user = new ApmUser();
                    user.setEmpNo(apmFlowInstanceRoleUser.getUserNo());
                    userList.add(user);
                }
            }
            apmRoleUserAO.setUserList(userList);
            apmRoleUserAOList.add(apmRoleUserAO);
        });
        return apmRoleUserAOList;
    }

    @Override
    public List<ApmRoleUserAO> listInstanceRoleUser(String instanceBid) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        List<String> roleBids = apmFlowInstanceNodes.stream().map(ApmFlowInstanceNode::getNodeRoleBids).filter(CollectionUtils::isNotEmpty).flatMap(List::stream).distinct().collect(Collectors.toList());
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isEmpty(apmFlowInstanceRoleUsers)) {
            return Lists.newArrayList();
        }
        return buildRoleUserAOList(apmFlowInstanceRoleUsers, roleBids);
    }

    /**
     * 查询节点对应角色用户关系
     */
    private Map<String, Set<String>> queryNodeUserMap(String instanceBid) {
        Map<String, Set<String>> nodeUserMap = Maps.newHashMap();
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBid(instanceBid);
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        apmFlowInstanceNodes.forEach(e -> {
            List<String> nodeRoleBids = e.getNodeRoleBids();
            if (CollectionUtils.isNotEmpty(nodeRoleBids)) {
                Set<String> roleUsers = apmFlowInstanceRoleUsers.stream().filter(roleUser -> nodeRoleBids.contains(roleUser.getRoleBid()))
                        .map(ApmFlowInstanceRoleUser::getUserNo).collect(Collectors.toSet());
                nodeUserMap.put(e.getBid(), roleUsers);
            }
        });
        return nodeUserMap;
    }

    /**
     * 删除实例时移除实例流程下所有待办
     */
    private void removeInstanceTodoTask(String instanceBid) {
        Map<String, Set<String>> nodeUserMap = queryNodeUserMap(instanceBid);
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByBids(new ArrayList<>(nodeUserMap.keySet()));
        if (CollectionUtils.isNotEmpty(apmFlowInstanceNodes)) {
            ApmFlowInstanceNode apmFlowInstanceNode = apmFlowInstanceNodes.get(0);
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), false);
            String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mSpaceAppData.getSpaceBid(), apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid());
            String content = String.format("您有一个新的任务需要前往【%s】工作台处理", todoCenterAppName);
            apmFlowInstanceNodes.forEach(e -> {
                Set<String> userNos = nodeUserMap.get(e.getBid());
                if (CollectionUtils.isNotEmpty(userNos)) {
                    CompletableFuture.runAsync(() -> toDoCenterService.pushTodoTaskState(e, mSpaceAppData, Lists.newArrayList(userNos), content, url, TodoCenterConstant.DELETE, TodoCenterConstant.APPROVAL_COMPLETE), SimpleThreadPool.getInstance());
                }
            });
        }
    }

    @Override
    public Boolean updateRoleUserByLifeCycleCode(String instanceBid, String lifeCycleCode, List<ApmRoleUserAO> roleUserList) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        Assert.hasText(lifeCycleCode, "lifeCycleCode不能为空");
        if (CollectionUtils.isEmpty(roleUserList)) {
            return Boolean.TRUE;
        }
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = Lists.newArrayList();
        List<String> updatedRoleBids = Lists.newArrayList();
        roleUserList.forEach(apmRoleUserAO -> {
            updatedRoleBids.add(apmRoleUserAO.getRoleBid());
            if (CollectionUtils.isEmpty(apmRoleUserAO.getUserList())) {
                return;
            }
            apmRoleUserAO.getUserList().forEach(apmUser -> {
                ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
                apmFlowInstanceRoleUser.setRoleBid(apmRoleUserAO.getRoleBid());
                apmFlowInstanceRoleUser.setUserNo(apmUser.getEmpNo());
                apmFlowInstanceRoleUser.setLifeCycleCode(lifeCycleCode);
                apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
            });
        });
        log.info("批量删除实例角色人员，并插入新数据，实例：{}，角色：{}", instanceBid, updatedRoleBids);
        //根据instanceBid和roleBids删除
        apmFlowInstanceRoleUserService.deleteByInstanceBidAndLifeCycleCode(instanceBid, lifeCycleCode, updatedRoleBids);
        //批量插入人员角色
        apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
        return true;
    }

    private ApmRoleUserAO getApmRoleUserAO(String roleBid, String empNo, List<String> empNos) {
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(roleBid);
        List<ApmUser> userList = new ArrayList<>();
        if (empNo != null) {
            ApmUser apmUser = new ApmUser();
            apmUser.setEmpNo(empNo);
            userList.add(apmUser);
        }
        if (CollectionUtils.isNotEmpty(empNos)) {
            List<String> existList = new ArrayList<>();
            for (String empNo1 : empNos) {
                if (!existList.contains(empNo1)) {
                    ApmUser apmUser = new ApmUser();
                    apmUser.setEmpNo(empNo1);
                    userList.add(apmUser);
                    existList.add(empNo1);
                }
            }
        }
        apmRoleUserAO.setUserList(userList);
        return apmRoleUserAO;
    }


    private List<String> getObjectList(Object object) {
        List<String> list = new ArrayList<>();
        if (object == null) {
            return list;
        }
        if (object instanceof List) {
            list = JSON.parseArray(object.toString(), String.class);
        } else if (object instanceof String) {
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if (StringUtils.isNotEmpty(objectStr)) {
                list.add(objectStr);
            }
        } else {
            if (object.toString().startsWith(CommonConstant.OPEN_BRACKET)) {
                list = JSON.parseArray(object.toString(), String.class);
            } else {
                list.add(object.toString());
            }
        }
        return list;
    }


    @Override
    public Boolean updateRoleUser(String instanceBid, ApmFlowQo qo) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        List<ApmRoleUserAO> roleUserList = qo.getRoleUserList();
        if (CollectionUtils.isEmpty(roleUserList)) {
            return Boolean.TRUE;
        }
        com.transcend.plm.datadriven.common.tool.Assert.isTrue(
                roleUserList.stream().map(ApmRoleUserAO::getUserList).filter(Objects::nonNull)
                        .mapToLong(Collection::size).sum() > 0,
                "请至少保留一个处理人"
        );

        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = Lists.newArrayList();
        List<String> updatedRoleBids = Lists.newArrayList();
        roleUserList.forEach(apmRoleUserAO -> {
            updatedRoleBids.add(apmRoleUserAO.getRoleBid());
            if (CollectionUtils.isEmpty(apmRoleUserAO.getUserList())) {
                return;
            }
            apmRoleUserAO.getUserList().forEach(apmUser -> {
                ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
                apmFlowInstanceRoleUser.setRoleBid(apmRoleUserAO.getRoleBid());
                apmFlowInstanceRoleUser.setSpaceBid(qo.getSpaceBid());
                apmFlowInstanceRoleUser.setSpaceAppBid(qo.getSpaceAppBid());
                apmFlowInstanceRoleUser.setUserNo(apmUser.getEmpNo());
                apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
            });
        });
        // 查询修改前的人员
        List<ApmFlowInstanceRoleUser> oldRoleUser = apmFlowInstanceRoleUserService.listByInstanceBid(instanceBid).stream().
                filter(e -> updatedRoleBids.contains(e.getRoleBid())).collect(Collectors.toList());
        // 查询节点
        List<ApmFlowInstanceNode> changeNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid).stream()
                .filter(e -> CollectionUtils.isNotEmpty(e.getNodeRoleBids()) &&
                        e.getNodeRoleBids().stream().anyMatch(updatedRoleBids::contains))
                .collect(Collectors.toList());

        log.info("批量删除实例角色人员，并插入新数据，实例：{}，角色：{}", instanceBid, updatedRoleBids);
        //根据instanceBid和roleBids删除
        apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBids(instanceBid, updatedRoleBids);
        //批量插入人员角色
        apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
        // 移除原来人员的代办中心任务
        if (CollectionUtils.isNotEmpty(changeNodes)) {
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(qo.getSpaceAppBid(), instanceBid, false);
            String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, mSpaceAppData.getSpaceBid(), qo.getSpaceAppBid(), instanceBid);
            String content = String.format("您有一个新的任务需要前往【%s】工作台处理", todoCenterAppName);
            changeNodes.forEach(e -> {
                List<String> nodeRoleBids = e.getNodeRoleBids();
                List<String> userNos = oldRoleUser.stream().filter(o -> nodeRoleBids.contains(o.getRoleBid())).map(ApmFlowInstanceRoleUser::getUserNo).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(userNos)) {
                    CompletableFuture.runAsync(() -> toDoCenterService.pushTodoTaskState(e, mSpaceAppData, Lists.newArrayList(userNos), content, url, TodoCenterConstant.DELETE, TodoCenterConstant.APPROVAL_COMPLETE), SimpleThreadPool.getInstance());
                }
            });
        }
        //获取当前实例的激活节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBidAndState(instanceBid, FlowNodeStateConstant.ACTIVE);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return Boolean.TRUE;
        }
        //处理节点的任务
        apmFlowInstanceNodes.forEach(this::createTask);
        return true;
    }

    @Override
    public Boolean updateTonesRoleUser(String instanceBid, ApmFlowQo qo) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        List<ApmRoleUserAO> roleUserList = qo.getRoleUserList();
        if (CollectionUtils.isEmpty(roleUserList)) {
            return Boolean.TRUE;
        }
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = Lists.newArrayList();
        List<String> updatedRoleBids = Lists.newArrayList();
        roleUserList.forEach(apmRoleUserAO -> {
            updatedRoleBids.add(apmRoleUserAO.getRoleBid());
            if (CollectionUtils.isEmpty(apmRoleUserAO.getUserList())) {
                return;
            }
            apmRoleUserAO.getUserList().forEach(apmUser -> {
                ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
                apmFlowInstanceRoleUser.setRoleBid(apmRoleUserAO.getRoleBid());
                apmFlowInstanceRoleUser.setSpaceBid(qo.getSpaceBid());
                apmFlowInstanceRoleUser.setSpaceAppBid(qo.getSpaceAppBid());
                apmFlowInstanceRoleUser.setUserNo(apmUser.getEmpNo());
                apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
            });
        });
        log.info("批量删除实例角色人员，并插入新数据，实例：{}，角色：{}", instanceBid, updatedRoleBids);
        //根据instanceBid和roleBids删除
        apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBidsToIds(instanceBid, updatedRoleBids);
        //批量插入人员角色
        apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProcess(String instanceBid) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        removeInstanceTodoTask(instanceBid);
        //根据instanceBid删除流程实例角色人员
        apmFlowInstanceRoleUserService.deleteByInstanceBid(instanceBid);
        //查询流程实例节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return Boolean.TRUE;
        }
        List<String> nodeBids = apmFlowInstanceNodes.stream().map(ApmFlowInstanceNode::getBid).collect(Collectors.toList());
        apmTaskApplicationService.deleteByBizBids(TaskTypeConstant.FLOW, nodeBids);
        //根据instanceBid删除流程实例节点
        return apmFlowInstanceNodeService.deleteByInstanceBid(instanceBid);
    }

    @Override
    public Boolean deleteProcess(List<String> instanceBids) {
        instanceBids.forEach(this::removeInstanceTodoTask);
        //批量删除流程实例角色人员
        apmFlowInstanceRoleUserService.deleteByInstanceBids(instanceBids);
        //批量查询流程实例节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBids(instanceBids);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return Boolean.TRUE;
        }
        List<String> nodeBids = apmFlowInstanceNodes.stream().map(ApmFlowInstanceNode::getBid).collect(Collectors.toList());
        apmTaskApplicationService.deleteByBizBids(TaskTypeConstant.FLOW, nodeBids);
        apmTaskApplicationService.deleteByBizBids(TaskTypeConstant.STATE, nodeBids);
        //根据instanceBid删除流程实例节点
        return apmFlowInstanceNodeService.deleteByInstanceBids(instanceBids);
    }

    /**
     * 获取当前节点的所有后续节点
     *
     * @param nodeBid              当前节点bid
     * @param apmFlowInstanceNodes 流程实例节点列表
     * @return 后续节点列表
     */
    private List<ApmFlowInstanceNode> getSubsequentNodes(String nodeBid, List<ApmFlowInstanceNode> apmFlowInstanceNodes, List<String> nodeBidList) {
        List<ApmFlowInstanceNode> result = Lists.newArrayList();
        List<ApmFlowInstanceNode> nextNodes = getNextNodes(nodeBid, apmFlowInstanceNodes, nodeBidList);
        if (CollectionUtils.isEmpty(nextNodes)) {
            return Lists.newArrayList();
        }
        //返回的下一个节点是否包含进行中节点 如果是 只需要进行中节点
        if (nextNodes.stream().anyMatch(node -> FlowNodeStateConstant.ACTIVE.equals(node.getNodeState()))) {
            result.addAll(nextNodes.stream().filter(node -> FlowNodeStateConstant.ACTIVE.equals(node.getNodeState())).collect(Collectors.toList()));
            return result;
        }
        result.addAll(nextNodes);
        for (ApmFlowInstanceNode nextNode : nextNodes) {
            result.addAll(getSubsequentNodes(nextNode.getBid(), apmFlowInstanceNodes, nodeBidList));
        }
        return result;
    }

    public boolean activeNode(ApmFlowInstanceNode apmFlowInstanceNode, boolean runEvent) {
        log.info("执行前置事件,{}", apmFlowInstanceNode.getBid());
        if (runEvent) {
            /*NotifyEventBus.EVENT_BUS.post(
                    NodeExecuteBusEvent.builder()
                            .eventType(FlowEventTypeConstant.BEFORE_ACTIVE)
                            .instanceNode(apmFlowInstanceNode)
                            .completeEmpName(SsoHelper.getName())
                            .completeEmpNO(SsoHelper.getJobNumber())
                            .build()
            );*/
            NodeExecuteBusEvent nodeExecuteBusEvent = NodeExecuteBusEvent.builder()
                    .eventType(FlowEventTypeConstant.BEFORE_ACTIVE)
                    .instanceNode(apmFlowInstanceNode)
                    .completeEmpName(SsoHelper.getName())
                    .completeEmpNO(SsoHelper.getJobNumber())
                    .build();
            CompletableFuture.runAsync(() -> executeFlowEvent(nodeExecuteBusEvent), SimpleThreadPool.getInstance());
        }
        apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.ACTIVE);
        //自动完成类型节点，则自动完成
        if (FlowNodeCompleteType.AUTO_COMPLETE.equals(apmFlowInstanceNode.getComplateType())) {
            log.info("自动节点，自动完成,{}", apmFlowInstanceNode.getBid());
            completeNode(apmFlowInstanceNode.getBid(), null);
            apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.COMPLETED);
        }
        log.info("生成工作台任务,{}", apmFlowInstanceNode.getBid());
        createTask(apmFlowInstanceNode);
        return true;
    }

    private void executeFlowEvent(NodeExecuteBusEvent nodeExecuteBusEvent) {
        ApmFlowInstanceNode apmFlowInstanceNode = nodeExecuteBusEvent.getInstanceNode();
        List<ApmFlowNodeEvent> apmFlowNodeEvents = apmFlowApplicationService.listNodeEvents(nodeExecuteBusEvent.getEventType(), apmFlowInstanceNode.getTemplateNodeBid(), apmFlowInstanceNode.getVersion());
        if (CollectionUtils.isEmpty(apmFlowNodeEvents)) {
            return;
        }
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        apmFlowNodeEvents.forEach(apmFlowNodeEvent -> {
            if (apmFlowNodeEvent.getEventType() == FlowEventTypeConstant.NODE_STATUS) {
                //改状态
                mSpaceAppData.setLifeCycleCode(apmFlowNodeEvent.getFiledValue());
            } else if (apmFlowNodeEvent.getEventType() == FlowEventTypeConstant.MODIFY_FIELD) {
                //改字段
                if (FlowEnum.FLOW_EVENT_NOW.getCode().equals(apmFlowNodeEvent.getFiledValueType())) {
                    mSpaceAppData.put(apmFlowNodeEvent.getFiledName(), DateUtil.formatDateTime(new Date()));
                } else if (FlowEnum.FLOW_EVENT_LOGINUSER.getCode().equals(apmFlowNodeEvent.getFiledValueType())) {
                    mSpaceAppData.put(apmFlowNodeEvent.getFiledName(), nodeExecuteBusEvent.getCompleteEmpNO());
                } else {
                    mSpaceAppData.put(apmFlowNodeEvent.getFiledName(), apmFlowNodeEvent.getFiledValue());
                }
            } else {
                IFlowEventHandler flowEventHandler = FlowEventHandlerFactory.getHandler(apmFlowNodeEvent.getEventType());
                if (flowEventHandler == null) {
                    log.error("未定义的事件类型:{}", apmFlowNodeEvent.getEventType());
                    return;
                }
                FlowEventBO flowEventBO = new FlowEventBO();
                flowEventBO.setInstanceNode(apmFlowInstanceNode);
                flowEventBO.setCompleteEmpNO(nodeExecuteBusEvent.getCompleteEmpNO());
                flowEventBO.setEvent(apmFlowNodeEvent);
                flowEventBO.setPageMSpaceAppData(nodeExecuteBusEvent.getPageMSpaceAppData());
                flowEventHandler.handle(flowEventBO);
            }
        });
        if (mSpaceAppData != null && mSpaceAppData.size() > 0) {
            mSpaceAppData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(),false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), mSpaceAppData);
        }
    }

    public boolean completeNode(ApmFlowInstanceNode apmFlowInstanceNode,MSpaceAppData pageData) {
        apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.COMPLETED);
        apmFlowInstanceNodeService.updateById(apmFlowInstanceNode);
//        saveOperationLog("完成", apmFlowInstanceNode);
        /*NotifyEventBus.EVENT_BUS.post(
                NodeExecuteBusEvent.builder()
                        .eventType(FlowEventTypeConstant.AFTER_COMPLETE)
                        .instanceNode(apmFlowInstanceNode)
                        .completeEmpNO(SsoHelper.getJobNumber())
                        .completeEmpName(SsoHelper.getName())
                        .build()
        );*/
        NodeExecuteBusEvent nodeExecuteBusEvent = NodeExecuteBusEvent.builder()
                .eventType(FlowEventTypeConstant.AFTER_COMPLETE)
                .instanceNode(apmFlowInstanceNode)
                .completeEmpNO(SsoHelper.getJobNumber())
                .completeEmpName(SsoHelper.getName())
                .pageMSpaceAppData(pageData)
                .build();
        CompletableFuture.runAsync(() -> executeFlowEvent(nodeExecuteBusEvent), SimpleThreadPool.getInstance());
        return true;
    }

    public boolean cancelActiveNode(ApmFlowInstanceNode apmFlowInstanceNode) {
        apmFlowInstanceNode.setNodeState(FlowNodeStateConstant.NOT_START);
        ApmTaskDeleteAO taskDeleteAO = new ApmTaskDeleteAO();
        taskDeleteAO.setTaskType(TaskTypeConstant.FLOW);
        taskDeleteAO.setBizBids(Lists.newArrayList(apmFlowInstanceNode.getBid()));
        return apmTaskApplicationService.deleteByApmTaskDeleteAO(taskDeleteAO);
    }

    @Subscribe
    private void executeEvent(NodeExecuteBusEvent nodeExecuteBusEvent) {
        try {
            //休眠，防止数据还为保存好就执行修改事件
            Thread.sleep(2500);
        } catch (Exception e) {

        }
        ApmFlowInstanceNode apmFlowInstanceNode = nodeExecuteBusEvent.getInstanceNode();
        List<ApmFlowNodeEvent> apmFlowNodeEvents = apmFlowApplicationService.listNodeEvents(nodeExecuteBusEvent.getEventType(), apmFlowInstanceNode.getTemplateNodeBid(), apmFlowInstanceNode.getVersion());
        if (CollectionUtils.isEmpty(apmFlowNodeEvents)) {
            return;
        }
        apmFlowNodeEvents.forEach(apmFlowNodeEvent -> {
            IFlowEventHandler flowEventHandler = FlowEventHandlerFactory.getHandler(apmFlowNodeEvent.getEventType());
            if (flowEventHandler == null) {
                log.error("未定义的事件类型:{}", apmFlowNodeEvent.getEventType());
                return;
            }
            FlowEventBO flowEventBO = new FlowEventBO();
            flowEventBO.setInstanceNode(apmFlowInstanceNode);
            flowEventBO.setCompleteEmpNO(nodeExecuteBusEvent.getCompleteEmpNO());
            flowEventBO.setEvent(apmFlowNodeEvent);
            flowEventHandler.handle(flowEventBO);
        });
    }

    @Override
    public void createOlnyTask(ApmFlowInstanceNode apmFlowInstanceNode) {
        if (FlowNodeCompleteType.AUTO_COMPLETE.equals(apmFlowInstanceNode.getComplateType())) {
            return;
        }
        log.info("生成节点任务，节点：{}", apmFlowInstanceNode.getBid());
        ApmTaskAO apmTaskAO = buildTaskAO(apmFlowInstanceNode);
        if (apmTaskAO == null) {
            return;
        }
        List<String> empNos = apmTaskApplicationService.saveOrUpdateApmTask(apmTaskAO);
        if (CollectionUtils.isEmpty(empNos)) {
            return;
        }
    }

    @Override
    public void startStateFlow(String spaceBid, String spaceAppBid, MObject mObject) {
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.list(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getSpaceAppBid, spaceAppBid).eq(ApmFlowTemplate::getType, "state"));
        if(com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(apmFlowTemplateList)){
            return;
        }
        ApmFlowTemplate apmFlowTemplate = apmFlowTemplateList.get(0);
        //查询所有节点
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowTemplate.getBid(), apmFlowTemplate.getVersion());
        if(com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(apmFlowTemplateNodes)){
            return;
        }
        //找到进行中的节点
        ApmFlowTemplateNode startNode = apmFlowTemplateNodes.stream().filter(v -> v.getLifeCycleCode().equals(mObject.getLifeCycleCode())).findFirst().orElse(null);
        //保存实例流程节点
        List<ApmFlowTemplateNodeVO> nodeVOS = AmpFlowTemplateNodeConerter.INSTANCE.entitys2Vos(apmFlowTemplateNodes);
        for(ApmFlowTemplateNodeVO apmFlowTemplateNodeVO:nodeVOS){
            apmFlowTemplateNodeVO.setSpaceAppBid(apmFlowTemplate.getSpaceAppBid());
            if(apmFlowTemplateNodeVO.getNodeType().equals(FlowNodeTypeConstant.START_NODE)){
                apmFlowTemplateNodeVO.setLayout(apmFlowTemplate.getLayout());
            }
        }
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = generateInstanceNodesFromTemplate(mObject.getBid(), nodeVOS);
        if (startNode != null) {
            apmFlowInstanceNodes.stream().filter(v->v.getTemplateNodeBid().equals(startNode.getBid())).forEach(v->v.setNodeState(FlowNodeStateConstant.ACTIVE));
        }
        //避免重复生成，如果存在实例节点，则不生成
        List<ApmFlowInstanceNode> existInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(mObject.getBid());
        if (CollectionUtils.isNotEmpty(existInstanceNodes)) {
           return;
        }
        apmFlowInstanceNodeService.saveBatch(apmFlowInstanceNodes);
        //生成状态待办
        StateFlowTodoDriveAO stateFlowTodoDriveAO = new StateFlowTodoDriveAO();
        stateFlowTodoDriveAO.setInstanceBid(mObject.getBid());
        stateFlowTodoDriveAO.setInstanceName(mObject.getName());
        List<ApmFlowNodeLine> apmFlowNodeLineList = apmFlowNodeLineService.listNodeLinesByTempBidAndVersion(apmFlowTemplate.getBid(), apmFlowTemplate.getVersion());
        Object finalLifeCycleCode = mObject.getLifeCycleCode();
        stateFlowTodoDriveAO.setIsLastState(com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(apmFlowNodeLineList) || apmFlowNodeLineList.stream().noneMatch(v->v.getSourceNodeCode().equals(finalLifeCycleCode)));
        stateFlowTodoDriveAO.setLifeCycleCode(mObject.getLifeCycleCode());
        stateFlowTodoDriveAO.setSpaceBid(spaceBid);
        stateFlowTodoDriveAO.setSpaceAppBid(spaceAppBid);
        stateFlowTodoDriveAO.setModelCode(mObject.getModelCode());
        List<String> personResponsible = new ArrayList<>();
        Object personResponsibleObject = mObject.get(ObjectEnum.HANDLER.getCode());
        if (ObjectUtil.isEmpty(personResponsibleObject)) {
            return;
        }
        if(personResponsibleObject instanceof List){
            try{
                personResponsible = JSON.parseArray(JSON.toJSONString(personResponsibleObject), String.class);
            }catch (Exception e){
                log.error("checkPermission error",e);
            }
        }else if(personResponsibleObject instanceof String){
            personResponsible = Lists.newArrayList(personResponsibleObject.toString());
        }
        stateFlowTodoDriveAO.setPersonResponsible(new HashSet<>(personResponsible));
        NotifyEventBus.EVENT_BUS.post(stateFlowTodoDriveAO);
    }

    @Override
    public List<ApmFlowTemplateNode> listKeyLifeCycleCodes(ApmStateQo apmStateQo) {
        //查询实例流程节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(apmStateQo.getInstanceBid());
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return new ArrayList<>();
        }
        //查询流程模板
        ApmFlowTemplate flowTemplate = apmFlowTemplateService.getByBid(apmFlowInstanceNodes.get(0).getFlowTemplateBid());
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowInstanceNodes.get(0).getFlowTemplateBid(), apmFlowInstanceNodes.get(0).getVersion());
        //List<ApmFlowNodeLine> apmFlowNodeLines = apmFlowNodeLineService.listNodeLinesByTempBidAndVersion(apmFlowInstanceNodes.get(0).getFlowTemplateBid(), apmFlowInstanceNodes.get(0).getVersion());
        List<ApmFlowNodeLine> apmFlowNodeLines = getNodeLines(apmFlowTemplateNodes);
        //查询流程模板关键节点列表
        List<ApmFlowTemplateNode> apmFlowTemplateKeyPathNodes = getKeyTemplateNodeList(apmFlowTemplateNodes, apmFlowNodeLines);
        if (CollectionUtils.isEmpty(apmFlowTemplateKeyPathNodes)) {
            return new ArrayList<>();
        }
        //设置关键节点状态
        setNodeState(apmFlowInstanceNodes, apmFlowTemplateKeyPathNodes, apmFlowTemplateNodes, apmFlowNodeLines);
        //设置关键节点生命周期
        Map<String, String> nodeBidLifeCycleCodeMap = getFlowNodeLigeCycleCodeMap(flowTemplate, apmFlowTemplateNodes);
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(apmStateQo.getSpaceAppBid());
        TemplateDto templateDto = new TemplateDto();
        templateDto.setModelCode(spaceApp.getModelCode());
        templateDto.setTemplateBid(apmStateQo.getLcTemplBid());
        templateDto.setVersion(apmStateQo.getLcTemplVersion());
        List<CfgLifeCycleTemplateNodeVo> lifeCycleTemplateNodePos = lifeCycleFeignClient.getTemplateNodes(templateDto).getCheckExceptionData();
        Map<String, String> lifeCycleCodeMap = lifeCycleTemplateNodePos.stream().collect(Collectors.toMap(CfgLifeCycleTemplateNodeVo::getLifeCycleCode, CfgLifeCycleTemplateNodeVo::getName, (k1, v1) -> v1));
        setLiftCycleCode(apmFlowTemplateKeyPathNodes, nodeBidLifeCycleCodeMap, lifeCycleCodeMap);
        //相同并且连续的生命周期去重
        apmFlowTemplateKeyPathNodes = distinctLiftCycleCode(apmFlowTemplateKeyPathNodes);
        return apmFlowTemplateKeyPathNodes;
    }

    /**
     * 查看可回退的节点 集合
     *
     * @param instanceBid    instanceBid
     * @return List<ApmFlowInstanceNodeVO>
     */
    @Override
    public List<ApmFlowInstanceNodeVO> listRollbackNode(String instanceBid) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);

        // 过滤掉不允许回退的节点 + 过滤已经走过的节点，收集VO作为结果
        return apmFlowInstanceNodes.stream().filter(v ->{

            // 是否允许回退 未完成节点 || (过滤掉不允许回退的节点 + 过滤已经走过的节点)
            return Boolean.TRUE.equals(v.getAllowBackNotCompleted()) ||
                    (FlowNodeStateConstant.COMPLETED.equals(v.getNodeState()) && Boolean.TRUE.equals(v.getAllowBack()));
        }).map(

                // 转换为VO
                ApmFlowInstanceNodeConverter.INSTANCE::po2Vo
        ).collect(Collectors.toList());
    }

    @Override
    public Boolean copyNodeState(String modelCode,String sourceInstanceBid, List<String> targetInstanceBids) {
        //更新流程状态
        List<ApmFlowInstanceNode> sourceNodeState = apmFlowInstanceNodeService.listByInstanceBid(sourceInstanceBid);
        List<ApmFlowInstanceNode> targetNodeState = apmFlowInstanceNodeService.listByInstanceBids(targetInstanceBids);
        if (CollectionUtils.isEmpty(sourceNodeState) || CollectionUtils.isEmpty(targetNodeState)) {
            return Boolean.TRUE;
        }
        Map<String, Integer> nodeStateMap = sourceNodeState.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getWebBid, ApmFlowInstanceNode::getNodeState));
        for (ApmFlowInstanceNode apmFlowInstanceNode : targetNodeState) {
            if (nodeStateMap.containsKey(apmFlowInstanceNode.getWebBid())) {
                apmFlowInstanceNode.setNodeState(nodeStateMap.get(apmFlowInstanceNode.getWebBid()));
            }else if (FlowStateConstant.RUNNING.equals(apmFlowInstanceNode.getNodeState())) {
                apmFlowInstanceNode.setNodeState(FlowStateConstant.NOT_START);
            }
        }
        return apmFlowInstanceNodeService.updateBatchById(targetNodeState);

    }

    private List<ApmFlowTemplateNode> distinctLiftCycleCode(List<ApmFlowTemplateNode> apmFlowTemplateKeyPathNodes) {
        List<ApmFlowTemplateNode> res = new ArrayList<>();
        String lifeCycleCode = "";
        for (ApmFlowTemplateNode apmFlowTemplateKeyPathNode : apmFlowTemplateKeyPathNodes) {
            if (StringUtils.isNotEmpty(apmFlowTemplateKeyPathNode.getLifeCycleCode()) && apmFlowTemplateKeyPathNode.getLifeCycleCode().equals(lifeCycleCode)) {
                if (FlowStateConstant.RUNNING.equals(apmFlowTemplateKeyPathNode.getNodeState())) {
                    res.get(res.size() - 1).setNodeState(FlowStateConstant.RUNNING);
                }
            }else {
                res.add(apmFlowTemplateKeyPathNode);
                lifeCycleCode = apmFlowTemplateKeyPathNode.getLifeCycleCode();
            }
        }
        return res;
    }

    private void setLiftCycleCode(List<ApmFlowTemplateNode> apmFlowTemplateKeyPathNodes, Map<String, String> nodeBidLifeCycleCodeMap, Map<String, String> lifeCycleCodeMap) {
        for (ApmFlowTemplateNode apmFlowTemplateKeyPathNode : apmFlowTemplateKeyPathNodes) {
            if (nodeBidLifeCycleCodeMap.containsKey(apmFlowTemplateKeyPathNode.getBid())) {
                String lifeCycleCode = nodeBidLifeCycleCodeMap.get(apmFlowTemplateKeyPathNode.getBid());
                apmFlowTemplateKeyPathNode.setLifeCycleCode(lifeCycleCode);
                if (lifeCycleCodeMap.containsKey(lifeCycleCode)) {
                    apmFlowTemplateKeyPathNode.setNodeName(lifeCycleCodeMap.get(lifeCycleCode));
                }
            }
        }
    }

    private List<ApmFlowNodeLine> getNodeLines(List<ApmFlowTemplateNode> apmFlowTemplateNodes) {
        List<ApmFlowNodeLine> apmFlowNodeLines = new ArrayList<>();
        for (ApmFlowTemplateNode apmFlowTemplateNode : apmFlowTemplateNodes) {
            if (CollectionUtils.isNotEmpty(apmFlowTemplateNode.getBeforeNodeBids())) {
                for (String beforeNodeBid : apmFlowTemplateNode.getBeforeNodeBids()) {
                    ApmFlowNodeLine apmFlowNodeLine = new ApmFlowNodeLine();
                    apmFlowNodeLine.setSourceNodeBid(beforeNodeBid);
                    apmFlowNodeLine.setTargetNodeBid(apmFlowTemplateNode.getBid());
                    apmFlowNodeLines.add(apmFlowNodeLine);
                }
            }
        }
       return apmFlowNodeLines;
    }

    private void setNodeState(List<ApmFlowInstanceNode> apmFlowInstanceNodes, List<ApmFlowTemplateNode> apmFlowTemplateKeyPathNodes, List<ApmFlowTemplateNode> apmFlowTemplateNodes, List<ApmFlowNodeLine> apmFlowNodeLines) {
        //找到进行中的节点
        Map<String, Integer> nodeStateMap = apmFlowInstanceNodes.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getTemplateNodeBid, ApmFlowInstanceNode::getNodeState, (v1, v2) -> v1));
        apmFlowTemplateKeyPathNodes.forEach(v->v.setNodeState(nodeStateMap.get(v.getBid())));
        ApmFlowInstanceNode currentInstanceNode = apmFlowInstanceNodes.stream().filter(v -> v.getNodeState().equals(FlowNodeStateConstant.ACTIVE)).findFirst().orElse(null);
        if(currentInstanceNode == null) {
            boolean isCompleted = false;
            //中间某些节点可能会跳过，如果有节点是已完成，则将之前的节点设置为已完成
            for (int i = apmFlowTemplateKeyPathNodes.size()-1; i >= 0; i--) {
                if (FlowNodeStateConstant.COMPLETED.equals(apmFlowTemplateKeyPathNodes.get(i).getNodeState())){
                    isCompleted = true;
                }
                if (isCompleted){
                    apmFlowTemplateKeyPathNodes.get(i).setNodeState(FlowNodeStateConstant.COMPLETED);
                }
            }
            return;
        }
        ApmFlowTemplateNode currentFlowNode = apmFlowTemplateNodes.stream().filter(v -> v.getBid().equals(currentInstanceNode.getTemplateNodeBid())).findFirst().orElse(null);
        currentFlowNode.setNodeState(FlowStateConstant.RUNNING);
        //如果进行中的节点是关键节点，则将之前的节点设置为已完成，之后的节点设置为未完成
        if (apmFlowTemplateKeyPathNodes.stream().anyMatch(v->v.getBid().equals(currentFlowNode.getBid()))){
            Integer initState = FlowStateConstant.COMPLETED;
            for (ApmFlowTemplateNode apmFlowTemplateKeyPathNode : apmFlowTemplateKeyPathNodes) {
                if(apmFlowTemplateKeyPathNode.getBid().equals(currentFlowNode.getBid())){
                    initState = FlowStateConstant.NOT_START;
                }else {
                    apmFlowTemplateKeyPathNode.setNodeState(initState);
                }
            }
            return;
        }
        //如果进行中的节点不是关键节点，则向上找到第一个已经完成的关键节点
        Set<String> keyNodeBids = apmFlowTemplateKeyPathNodes.stream().filter(v->FlowStateConstant.COMPLETED.equals(v.getNodeState())).map(ApmFlowTemplateNode::getBid).collect(Collectors.toSet());
        Map<String, Set<String>> lastNodeMap = apmFlowNodeLines.stream().collect(Collectors.groupingBy(ApmFlowNodeLine::getTargetNodeBid, Collectors.mapping(ApmFlowNodeLine::getSourceNodeBid, Collectors.toSet())));
        List<String> bids = Lists.newArrayList(currentFlowNode.getBid());
        //第一层循环只是限制循环次数，代替递归
        Set<String> currentKeyNodes = new HashSet<>();
        outerLoop:
        for (int i = 0; i < apmFlowTemplateNodes.size(); i++) {
            List<String> lastBids = new ArrayList<>();
            for (String bid : bids) {
                Set<String> bids2 = lastNodeMap.get(bid);
                if (bids2 != null) {
                    Collection<String> intersection = org.apache.commons.collections4.CollectionUtils.intersection(bids2, keyNodeBids);
                    if (org.springframework.util.CollectionUtils.isEmpty(intersection)) {
                        lastBids.addAll(bids2);
                    } else {
                        currentKeyNodes = new HashSet<>(intersection);
                        break outerLoop;
                    }
                }
            }
            if (org.springframework.util.CollectionUtils.isEmpty(lastBids)) {
                //没有找到关键节点，直接返回
                log.info("没有找到关键节点");
                break;
            }
            bids = lastBids;
        }
        //向上没有找到已完成的关键节点，则将当前节点添加到第一位
        if (currentKeyNodes.isEmpty()){
            apmFlowTemplateKeyPathNodes.add(0,currentFlowNode);
            return;
        }
        //如果向上找到已完成的关键节点，将当前节点插入找到的关键节点之后，之前的节点设置为已完成，之后的节点设置为未开始
        int index = 0;
        for (int i = 0; i < apmFlowTemplateKeyPathNodes.size(); i++) {
            if (currentKeyNodes.contains(apmFlowTemplateKeyPathNodes.get(i).getBid())) {
                index = i;
            }
        }
        apmFlowTemplateKeyPathNodes.add(index+1,currentFlowNode);
        for (int i = 0; i < apmFlowTemplateKeyPathNodes.size(); i++) {
            if (i<index+1){
                apmFlowTemplateKeyPathNodes.get(i).setNodeState(FlowStateConstant.COMPLETED);
            } else if (i>index+1) {
                apmFlowTemplateKeyPathNodes.get(i).setNodeState(FlowStateConstant.NOT_START);
            }
        }
    }

    private List<ApmFlowTemplateNode> getKeyTemplateNodeList(List<ApmFlowTemplateNode> apmFlowTemplateNodes, List<ApmFlowNodeLine> apmFlowNodeLines) {
        List<ApmFlowTemplateNode> keyNodes = apmFlowTemplateNodes.stream().filter(v -> Boolean.TRUE.equals(v.getKeyPathFlag())).collect(Collectors.toList());
        if (org.springframework.util.CollectionUtils.isEmpty(keyNodes)) {
            return Collections.emptyList();
        }
        keyNodes.sort(Comparator.comparingInt(o -> o.getSort() == null ? 0 : o.getSort()));
        /*Set<String> keyNodeBids = keyNodes.stream().map(ApmFlowTemplateNode::getBid).collect(Collectors.toSet());
        List<ApmFlowNodeLine> keyLine = apmFlowNodeLines.stream().filter(v -> keyNodeBids.contains(v.getSourceNodeBid()) && keyNodeBids.contains(v.getTargetNodeBid())).collect(Collectors.toList());
        if (org.springframework.util.CollectionUtils.isEmpty(keyLine)) {
            return Collections.emptyList();
        }
        Map<String, ApmFlowTemplateNode> keyNodeMap = keyNodes.stream().collect(Collectors.toMap(ApmFlowTemplateNode::getBid, a -> a, (k1, k2) -> k1));
        Map<String,Set<String>> linePreMap = new HashMap<>();
        Map<String,Set<String>> lineNextMap = new HashMap<>();
        keyLine.forEach(apmFlowNodeLine -> {
            if (keyNodeBids.contains(apmFlowNodeLine.getSourceNodeBid()) && keyNodeBids.contains(apmFlowNodeLine.getTargetNodeBid())) {
                if (linePreMap.containsKey(apmFlowNodeLine.getTargetNodeBid())){
                    linePreMap.get(apmFlowNodeLine.getTargetNodeBid()).add(apmFlowNodeLine.getSourceNodeBid());
                }else {
                    linePreMap.put(apmFlowNodeLine.getTargetNodeBid(),Sets.newHashSet(apmFlowNodeLine.getSourceNodeBid()));
                }
                if (lineNextMap.containsKey(apmFlowNodeLine.getSourceNodeBid())){
                    lineNextMap.get(apmFlowNodeLine.getSourceNodeBid()).add(apmFlowNodeLine.getTargetNodeBid());
                }else {
                    lineNextMap.put(apmFlowNodeLine.getSourceNodeBid(),Sets.newHashSet(apmFlowNodeLine.getTargetNodeBid()));
                }
            }
        });
        //确定关键节点头结点
        ApmFlowTemplateNode head = null;
        for (ApmFlowTemplateNode flowTemplateNode : keyNodes) {
            if(!linePreMap.containsKey(flowTemplateNode.getBid())){
                head = flowTemplateNode;
                break;
            }
        }
        if (head == null) {
            //如果存在闭环，找不到头节点，可以手动配置一个头节点
            if(CollectionUtils.isNotEmpty(keyNodeHeadDataBid)) {
                Map<String, ApmFlowTemplateNode> keyNodeDataBidMap = keyNodes.stream().collect(Collectors.toMap(ApmFlowTemplateNode::getDataBid, a -> a, (k1, k2) -> k1));
                for (String headDataBid : keyNodeHeadDataBid) {
                    if (keyNodeDataBidMap.containsKey(headDataBid)) {
                        head = keyNodeDataBidMap.get(headDataBid);
                        break;
                    }
                }
            }
        }
        cn.hutool.core.lang.Assert.notNull(head,"流程模板数据异常，找不到头节点,模板Bid={}",apmFlowTemplateNodes.get(0).getFlowTemplateBid());
        List<ApmFlowTemplateNode> resList = new ArrayList<>();
        resList.add(head);
        Set<String> sortedNodeBids = Sets.newHashSet();
        sortedNodeBids.add(head.getBid());
        String nextNodeBid  = head.getBid();
        for (int i = 0; i < keyNodes.size(); i++) {
            Set<String> nextNodeBids = lineNextMap.get(nextNodeBid);
            if (nextNodeBids == null || nextNodeBids.isEmpty()){
                break;
            }
            for (String nodeBid : nextNodeBids) {
                if (!sortedNodeBids.contains(nodeBid)) {
                    nextNodeBid = nodeBid;
                    sortedNodeBids.add(nodeBid);
                    resList.add(keyNodeMap.get(nextNodeBid));
                    break;
                }
            }
        }
        if(resList.size() != keyNodes.size()){
            //由于闭环或者孤立节点，手动设置一个顺序，下个迭代节点增加顺序
            Map<Object, Integer> sortMap = new HashMap<>();
            sortMap.put("1342184641798664192",0);
            sortMap.put("1344669916269740041",1);
            sortMap.put("1342184641798664195",2);
            sortMap.put("1342184641798664196",3);
            sortMap.put("1342184641798664197",4);
            sortMap.put("1344963199836721162",5);
            sortMap.put("1342184641798664198",6);
            sortMap.put("1342184641798664199",7);
            sortMap.put("1342184641798664200",8);
            sortMap.put("1342184641798664201",9);
            sortMap.put("1343997421372772360",10);

            sortMap.put("1342460448186101760",0);
            sortMap.put("1342460448186101761",1);
            sortMap.put("1342460448186101762",2);
            sortMap.put("1342460448186101763",3);
            sortMap.put("1342460448186101764",4);
            sortMap.put("1342460448186101765",5);
            sortMap.put("1342460448186101766",6);

            sortMap.put("1342465371460136960",0);
            sortMap.put("1342465371460136961",1);
            sortMap.put("1342465371460136962",2);
            sortMap.put("1342465371460136963",3);
            sortMap.put("1342465371460136964",4);
            sortMap.put("1342465371460136965",5);
            sortMap.put("1342465371460136966",6);

            sortMap.put("1345482226206994432",0);
            sortMap.put("1345482226206994433",1);
            sortMap.put("1345482226206994434",2);
            sortMap.put("1345482226206994435",3);
            sortMap.put("1345482226206994436",4);

            keyNodes.sort(Comparator.comparingInt(o -> sortMap.get(o.getDataBid())));
            return keyNodes;
            *//*log.error("流程模板数据异常，存在孤立节点后者闭环,模板Bid={}",apmFlowTemplateNodes.get(0).getFlowTemplateBid());
            return keyNodes;*//*
        }*/
        return keyNodes;
    }

    private Map<String, String> getFlowNodeLigeCycleCodeMap(ApmFlowTemplate flowTemplate, List<ApmFlowTemplateNode> apmFlowTemplateNodes) {
        Map<String, String> nodeBidLifeCycleCodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (flowTemplate.getType().equals(FlowTypeEnum.TASK.getCode())) {
            List<String> nodeBids = apmFlowTemplateNodes.stream().map(ApmFlowTemplateNode::getBid).collect(Collectors.toList());
            List<ApmFlowNodeEvent> nodeEventList = apmFlowNodeEventService.listByNodeBids(nodeBids);
            if (CollectionUtils.isNotEmpty(nodeEventList)) {
                for (ApmFlowNodeEvent apmFlowNodeEvent : nodeEventList) {
                    if (apmFlowNodeEvent.getEventType() == 1 && !nodeBidLifeCycleCodeMap.containsKey(apmFlowNodeEvent.getNodeBid())) {
                        nodeBidLifeCycleCodeMap.put(apmFlowNodeEvent.getNodeBid(), apmFlowNodeEvent.getFiledValue());
                    }
                    if (apmFlowNodeEvent.getEventType() == 2 && CommonConst.LIFE_CYCLE_CODE_STR.equals(apmFlowNodeEvent.getFiledName()) && !nodeBidLifeCycleCodeMap.containsKey(apmFlowNodeEvent.getNodeBid())) {
                        nodeBidLifeCycleCodeMap.put(apmFlowNodeEvent.getNodeBid(), apmFlowNodeEvent.getFiledValue());
                    }
                }
            }
        }else if (flowTemplate.getType().equals(FlowTypeEnum.STATE.getCode())) {
            for (ApmFlowTemplateNode apmFlowTemplateNode : apmFlowTemplateNodes) {
                nodeBidLifeCycleCodeMap.put(apmFlowTemplateNode.getBid(), apmFlowTemplateNode.getLifeCycleCode());
            }
        }
        return nodeBidLifeCycleCodeMap;
    }

    private void createTask(ApmFlowInstanceNode apmFlowInstanceNode) {
        if (FlowNodeCompleteType.AUTO_COMPLETE.equals(apmFlowInstanceNode.getComplateType())) {
            return;
        }
        log.info("生成节点任务，节点：{}", apmFlowInstanceNode.getBid());
        ApmTaskAO apmTaskAO = buildTaskAO(apmFlowInstanceNode);
        if (apmTaskAO == null) {
            return;
        }
        List<String> empNos = apmTaskApplicationService.saveOrUpdateApmTask(apmTaskAO);
        sendFeishu(apmFlowInstanceNode, empNos);
        //todo 推动待办中心
        /*String content = String.format("您有一个新的任务需要前往【%s】工作台处理", todoCenterAppName);
        toDoCenterService.pushTodoTaskData(apmFlowInstanceNode, mSpaceAppData, empNos, content, url);*/
    }

    public void sendFeishu(ApmFlowInstanceNode apmFlowInstanceNode, List<String> empNos) {
        if (flowTaskNotify && apmFlowInstanceNode.getNotifyFlag()) {
            if (CollectionUtils.isEmpty(empNos)) {
                return;
            }
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), false);
            sendFeishuMsg(mSpaceAppData.getSpaceBid(), mSpaceAppData.getSpaceAppBid(), mSpaceAppData.getBid(), mSpaceAppData.getName(), empNos, apmFlowInstanceNode.getNodeName());
        }
    }

    public void sendFeishu(ApmFlowInstanceNode apmFlowInstanceNode, List<String> empNos, String instanceName) {
        if (flowTaskNotify && apmFlowInstanceNode.getNotifyFlag()) {
            if (CollectionUtils.isEmpty(empNos)) {
                return;
            }
            ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(apmFlowInstanceNode.getSpaceAppBid());
            sendFeishuMsg(spaceApp.getSpaceBid(), spaceApp.getBid(), apmFlowInstanceNode.getInstanceBid(), instanceName, empNos, apmFlowInstanceNode.getNodeName());
        }
    }

    private void sendFeishuMsg(String spaceBid, String spaceAppBid, String instanceBid, String instanceName, List<String> empNos, String nodeName) {
        String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, spaceBid, spaceAppBid, instanceBid);
        //去掉零宽空格
        url = url.replaceAll("\\u200B","").trim();
        String title = "【" + todoCenterAppName + "】\ud83c\udff3\ud83c\udff4" + instanceName + "流程/" + nodeName + "任务";
        PushCenterFeishuBuilder feishuBuilder = PushCenterFeishuBuilder.builder().title(title, "red")
                .image(title, apmMsgPictureUrl)
                .dividingLine()
                .content("<font size='5'> 您有一个<font color='red'>新的任务</font>需要处理，请前往<font color='red'>【Transcend】</font>工作台处理\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25</font>")
                .dividingLine()
                .action(Lists.newArrayList(Button.builder().text(Text.builder().content("前往处理\uD83D\uDE80").build()).url(url).style("primary").build()))
                .url(url)
                .receivers(empNos);
        NotifyEventBus.EVENT_BUS.post(feishuBuilder);
    }

    /**
     * 战略供应商关系流程，ceg成员确认节点添加审批人
     *
     * @param: @param apmFlowInstanceNode * @param apmFlowInstanceNode
     * @return: java.util.List<java.lang.String>
     * @version: 1.0
     * @date: 2023/11/22/022
     * @author: yanbing.ao
     */
    private List<String> listNodeUserByCegConfirm(ApmFlowInstanceNode apmFlowInstanceNode) {
        List<String> empNoList = new ArrayList<>(8);
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(apmFlowInstanceNode.getSpaceAppBid());
        if (StrUtil.isEmpty(modelCode)) {
            return empNoList;
        }
        if (SSRMP_MODEL_CODE.equals(modelCode)) {
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), false);
            if (mSpaceAppData == null) {
                return empNoList;
            }
            List<Map<String, Object>> list = JSON.parseObject(String.valueOf(mSpaceAppData.get("memberTargetMaintenance")), new TypeReference<List<Map<String, Object>>>() {
            });
            if (CollectionUtils.isEmpty(list)) {
                return empNoList;
            }
            empNoList = list.stream().filter(e -> e.get("whetherParticipate") != null && (Integer) e.get("whetherParticipate") == 1).map(e -> String.valueOf(e.get("perno"))).distinct().collect(Collectors.toList());
            return empNoList;
        }
        return empNoList;
    }

    /**
     * 战略供应商关系流程，ceg团长节点添加审批人
     *
     * @param: @param apmFlowInstanceNode * @param apmFlowInstanceNode
     * @return: java.util.List<java.lang.String>
     * @version: 1.0
     * @date: 2023/11/22/022
     * @author: yanbing.ao
     */
    private List<String> listNodeUserByCegLeader(ApmFlowInstanceNode apmFlowInstanceNode) {
        List<String> empNoList = new ArrayList<>(8);
        String modelCode = apmSpaceApplicationService.getModelCodeBySpaceAppBid(apmFlowInstanceNode.getSpaceAppBid());
        if (StrUtil.isEmpty(modelCode)) {
            return empNoList;
        }
        if (SSRMP_MODEL_CODE.equals(modelCode)) {
            MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), false);
            if (mSpaceAppData == null) {
                return empNoList;
            }
            empNoList.add(mSpaceAppData.getCreatedBy());
            return empNoList;
        }
        return empNoList;
    }

    private ApmTaskAO buildTaskAO(ApmFlowInstanceNode apmFlowInstanceNode) {
        List<String> empNoList = new ArrayList<>(8);
        List<String> roleBids = Stream.concat(
                Optional.ofNullable(apmFlowInstanceNode.getNodeRoleBids()).orElse(Collections.emptyList()).stream(),
                Optional.ofNullable(apmFlowInstanceNode.getComplateRoleBids()).orElse(Collections.emptyList()).stream()
        ).collect(Collectors.toList());

        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndRoles(apmFlowInstanceNode.getInstanceBid(), roleBids);

        boolean addFlag = true;
        List<String> nodeRoleBids = apmFlowInstanceNode.getNodeRoleBids();
        if (CollectionUtils.isNotEmpty(nodeRoleBids)) {
            ApmRoleVO apmRoleVO = apmRoleService.getByBid(nodeRoleBids.get(0));
            if (apmRoleVO != null) {
                switch (apmRoleVO.getCode()) {
                    case "cegConfirm":
                        //战略供应商关系流程，ceg成员确认节点添加审批人
                        empNoList = listNodeUserByCegConfirm(apmFlowInstanceNode);
                        addFlag = false;
                        break;
                    case "cegLeader":
                        //战略供应商关系流程，ceg团长节点添加审批人
                        empNoList = listNodeUserByCegLeader(apmFlowInstanceNode);
                        addFlag = false;
                        break;
                    default:
                        break;
                }
            }
        }

        if (addFlag && CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)) {
            empNoList.addAll(apmFlowInstanceRoleUsers.stream().map(ApmFlowInstanceRoleUser::getUserNo).distinct().collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(empNoList)) {
            return null;
        }
        ApmTaskAO apmTaskAO = new ApmTaskAO();
        apmTaskAO.setBizBid(apmFlowInstanceNode.getBid());
        apmTaskAO.setTaskType(TaskTypeConstant.FLOW);
        apmTaskAO.setHandlers(empNoList);
        return apmTaskAO;
    }

    private void saveOperationLog(String type, ApmFlowInstanceNode apmFlowInstanceNode) {
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid(), false);
        GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(mSpaceAppData.getSpaceBid())
                .modelCode(mSpaceAppData.getModelCode())
                .instanceBid(apmFlowInstanceNode.getInstanceBid())
                .logMsg(type + " 节点任务 " + apmFlowInstanceNode.getNodeName())
                .build();
        operationLogEsService.genericSave(logParam);
    }

    /**
     * 查询流程实例的历程  TODO
     *
     * @param instanceBid 实例bid
     * @return List<ApmFlowInstanceProcessVo>
     */
    @Override
    public List<ApmFlowInstanceProcessVo> listInstanceProcess(String instanceBid) {

        return apmFlowInstanceProcessService.listByInstanceBid(instanceBid);
    }

    /**
     * 记录流程实例的历程 TODO
     *
     * @param dto 流程实例历程dto
     * @return Boolean
     */
    @Override
    public Boolean saveFlowProcess(ApmFlowInstanceProcessDto dto) {
        return apmFlowInstanceProcessService.save(dto);
    }

    /**
     * @param nodeBid
     * @param instanceBid
     * @param action
     * @param content
     * @return
     */
    @Override
    public Boolean saveFlowProcess(String nodeBid, String instanceBid, String action, Map<String, Object> content) {
        return saveFlowProcess(nodeBid, instanceBid, action, null, content);
    }


    /**
     * @param nodeBid
     * @param instanceBid
     * @param action
     * @param content
     * @return
     */
    @Override
    public Boolean saveFlowProcess(String nodeBid, String instanceBid, String action, ApmFlowInstanceNode apmFlowInstanceNode, Map<String, Object> content) {
        String nodeName = "开始";
        String processedActionName = null;
        String processedNodeName = null;
        String nextNodeName = null;
        if (null == apmFlowInstanceNode || StrUtil.isEmpty(apmFlowInstanceNode.getLineName())){
            Map<String, String> actionMap = new HashMap<>(8);
            actionMap.put("rollback", "退回");
            actionMap.put("start", "开始");
            actionMap.put("complete", "通过");
            processedActionName = actionMap.get(action);
        } else {
            processedActionName = apmFlowInstanceNode.getLineName();
            nextNodeName = apmFlowInstanceNode.getNodeName();
        }

        // 不为开始节点，需要查询当前节点数据
        if (!"start".equals(action)) {
            ApmFlowInstanceNode flowInstanceNode = apmFlowInstanceNodeService.getByBid(nodeBid);
            nodeName = flowInstanceNode.getNodeName();
        }
        processedNodeName = nodeName;
        // 节点名称 ： 当前节点 -> 下一步节点
        if(StringUtil.isNotBlank(nextNodeName)){
            processedNodeName = nodeName + " => " + nextNodeName;
        }
        ApmFlowInstanceProcessDto processDto = ApmFlowInstanceProcessDto.of()
                .setBid(SnowflakeIdWorker.nextIdStr())
                .setFlowInstanceBid(instanceBid)
                .setProcessedBy(SsoHelper.getJobNumber())
                .setProcessedByName(SsoHelper.getName())
                .setEndTime(new Date())
                .setNodeBid(nodeBid)
                .setNodeName(nodeName)
                .setProcessedAction(action)
                .setProcessedActionName(processedActionName)
                .setProcessedNodeName(processedNodeName)
                .setContent(content);
        return saveFlowProcess(processDto);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyEventBus.EVENT_BUS.register(this);
    }

    private ApmRoleUserAO getApmRoleUserAO(List<String> alyUserList, String spaceAppBid, String code) {
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return null;
        }
        List<String> codes = new ArrayList<>();
        codes.add(code);
        List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(codes, spaceSphere.getBid());
        if (CollectionUtils.isEmpty(apmRoleVOS)) {
            return null;
        }
        Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(codeBidMap.get(code));
        if (CollectionUtils.isNotEmpty(alyUserList)) {
            List<ApmUser> userList = new ArrayList<>();
            for (String userBid : alyUserList) {
                ApmUser apmUser = new ApmUser();
                apmUser.setEmpNo(userBid);
                userList.add(apmUser);
            }
            apmRoleUserAO.setUserList(userList);
        } else {
            apmRoleUserAO.setUserList(Lists.newArrayList());
        }
        return apmRoleUserAO;
    }

    /**
     * saveOrupdateFlowRoleUsers 无通知
     *
     * @param instanceBid instanceBid
     * @param alyUserList alyUserList
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return {@link boolean}
     */
    public boolean saveOrupdateFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code) {
        if(CollectionUtils.isEmpty(alyUserList) || StringUtils.isEmpty(instanceBid)){
            return true;
        }
        //查角色BID
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return false;
        }
        ApmRoleVO apmRoleVO = roleService.getByRoleBidsByCode(code, spaceSphere.getBid());
        if (apmRoleVO == null) {
            return false;
        }
        //删除原来的数据
        apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBidsToIds(instanceBid, Lists.newArrayList(apmRoleVO.getBid()));
        //新增数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        for (String user : alyUserList) {
            ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
            apmFlowInstanceRoleUser.setRoleBid(apmRoleVO.getBid());
            apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
            apmFlowInstanceRoleUser.setUserNo(user);
            apmFlowInstanceRoleUser.setSpaceBid(spaceBid);
            apmFlowInstanceRoleUser.setSpaceAppBid(spaceAppBid);
            apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
        }
        return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
    }
    /**
     * saveFlowRoleUsers 无通知
     *
     * @param instanceBid instanceBid
     * @param alyUserList alyUserList
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return {@link boolean}
     */
    public boolean saveFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code) {
        if(CollectionUtils.isEmpty(alyUserList) || StringUtils.isEmpty(instanceBid)){
            return true;
        }
        //查角色BID
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return false;
        }
        ApmRoleVO apmRoleVO = roleService.getByRoleBidsByCode(code, spaceSphere.getBid());
        if (apmRoleVO == null) {
            return false;
        }
        //查询原来的数据
        List<ApmFlowInstanceRoleUser> existRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndRoles(instanceBid, ListUtils.newArrayList(apmRoleVO.getBid()));
        List<String> existUsers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(existRoleUsers)) {
            existUsers = existRoleUsers.stream().map(ApmFlowInstanceRoleUser::getUserNo).collect(Collectors.toList());
        }
        //新增数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        for (String user : alyUserList) {
            if (!existUsers.contains(user)) {
                ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
                apmFlowInstanceRoleUser.setRoleBid(apmRoleVO.getBid());
                apmFlowInstanceRoleUser.setInstanceBid(instanceBid);
                apmFlowInstanceRoleUser.setUserNo(user);
                apmFlowInstanceRoleUser.setSpaceBid(spaceBid);
                apmFlowInstanceRoleUser.setSpaceAppBid(spaceAppBid);
                apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
            }
        }
        if (CollectionUtils.isEmpty(apmFlowInstanceRoleUsers)) {
            return true;
        }
        return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
    }


    @Override
    public boolean updateFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code) {
        //查角色BID
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return false;
        }
        List<String> codes = new ArrayList<>();
        codes.add(code);
        List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(codes, spaceSphere.getBid());
        if (CollectionUtils.isEmpty(apmRoleVOS)) {
            return false;
        }
        Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(codeBidMap.get(code));
        if (CollectionUtils.isNotEmpty(alyUserList)) {
            List<ApmUser> userList = new ArrayList<>();
            for (String userBid : alyUserList) {
                ApmUser apmUser = new ApmUser();
                apmUser.setEmpNo(userBid);
                userList.add(apmUser);
            }
            apmRoleUserAO.setUserList(userList);
        } else {
            apmRoleUserAO.setUserList(Lists.newArrayList());
        }
        apmRoleUserAOs.add(apmRoleUserAO);
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setRoleUserList(apmRoleUserAOs);
        apmFlowQo.setSpaceBid(spaceBid);
        apmFlowQo.setSpaceAppBid(spaceAppBid);
        return updateRoleUser(instanceBid, apmFlowQo);
    }

    @Override
    public boolean updateFlowRoleUsers(String instanceBid, String spaceBid, String spaceAppBid, Map<String, List<String>> roleUserMap) {
        //查角色BID
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return false;
        }
        List<String> codes = new ArrayList<>(roleUserMap.keySet());
        List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(codes, spaceSphere.getBid());
        if (CollectionUtils.isEmpty(apmRoleVOS)) {
            return false;
        }
        Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        for (Map.Entry<String, List<String>> entry : roleUserMap.entrySet()) {
            ApmRoleUserAO apmRoleUserAo = new ApmRoleUserAO();
            apmRoleUserAo.setRoleBid(codeBidMap.get(entry.getKey()));
            List<ApmUser> userList = Lists.newArrayList();
            for (String userBid : entry.getValue()) {
                ApmUser apmUser = new ApmUser();
                apmUser.setEmpNo(userBid);
                userList.add(apmUser);
            }
            apmRoleUserAo.setUserList(userList);
            apmRoleUserAOs.add(apmRoleUserAo);
        }
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setRoleUserList(apmRoleUserAOs);
        apmFlowQo.setSpaceBid(spaceBid);
        apmFlowQo.setSpaceAppBid(spaceAppBid);
        return updateRoleUser(instanceBid, apmFlowQo);
    }

    /**
     * 内置角色编码 修改
     *
     * @param instanceBid 实例bid
     * @param alyUserList 用户列表
     * @param spaceBid    空间bid
     * @param spaceAppBid 应用bid
     * @param code        角色编码
     * @return
     */
    public boolean updateFlowInnerRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code) {
        List<ApmRoleUserAO> apmRoleUserAOs = new ArrayList<>();
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(code);
        if (CollectionUtils.isNotEmpty(alyUserList)) {
            List<ApmUser> userList = new ArrayList<>();
            for (String userBid : alyUserList) {
                ApmUser apmUser = new ApmUser();
                apmUser.setEmpNo(userBid);
                userList.add(apmUser);
            }
            apmRoleUserAO.setUserList(userList);
        } else {
            apmRoleUserAO.setUserList(Lists.newArrayList());
        }
        apmRoleUserAOs.add(apmRoleUserAO);
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setRoleUserList(apmRoleUserAOs);
        apmFlowQo.setSpaceBid(spaceBid);
        apmFlowQo.setSpaceAppBid(spaceAppBid);
        return updateRoleUser(instanceBid, apmFlowQo);
    }

    private Map<String, String> getDictionaryMap(String code, String languageCode) {
        // 查询字典
        if (StringUtil.isBlank(languageCode)) {
            languageCode = LanguageEnum.ZH.getCode();
        }
        String finalLanguageCode = languageCode;
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(Lists.newArrayList(code));
        qo.setEnableFlags(Lists.newArrayList(CommonConst.ENABLE_FLAG_ENABLE));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtil.isNotEmpty(cfgDictionaryVos)) {
            Map<String, String> dictItemMap = cfgDictionaryVos.get(0).getDictionaryItems().stream()
                    .collect(Collectors.toMap(CfgDictionaryItemVo::getKeyCode, e -> (String) e.get(finalLanguageCode), (k1, k2) -> k1));
            return dictItemMap;
        }
        return new HashMap<>(CommonConstant.START_MAP_SIZE);
    }

    @Override
    public void setSpecialApmRoleUsers(String spaceAppBid, String instanceBid, MSpaceAppData mSpaceAppData) {
        if (piSpecialFlowSpaceAppBid.equals(spaceAppBid)) {
            //业务接口人角色特殊字段解析
            if (mSpaceAppData.containsKey(FIST_LEVEL_DEPARTMENT)) {
                //一级部门
                String firstLevelDepartment = mSpaceAppData.get(FIST_LEVEL_DEPARTMENT) + "";
                //制造中心
                if ("ManufacturingCenter".equals(firstLevelDepartment)) {
                    String busiUser = manufacturingCenterBusiUser;
                    if (StringUtils.isNotEmpty(busiUser)) {
                        List<String> alyUserList = Lists.newArrayList();
                        alyUserList.add(busiUser);
                        updateFlowRoleUsers(instanceBid, alyUserList, mSpaceAppData.getSpaceBid(), spaceAppBid, businessInterfacePersonRoleCode);
                    }
                }
            } else if (mSpaceAppData.containsKey(SECONDARY_DEPARTMENT)) {
                //二级部门 财务
                String secondLevelDepartment = mSpaceAppData.get(SECONDARY_DEPARTMENT) + "";
                Map<String, String> dictItemMap = getDictionaryMap("SECONDARY_DEPARTMENT", LanguageEnum.ZH.getCode());
                // 判断是否有财务的二级部门
                if (dictItemMap.containsKey(secondLevelDepartment)) {
                    Map<String, String> secondLevelDepartmentUserMap = getDictionaryMap("PI_FINANCE_DEP_BUSI_USER", LanguageEnum.EN.getCode());
                    String busiUser = secondLevelDepartmentUserMap.get("U_" + secondLevelDepartment);
                    if (StringUtils.isNotEmpty(busiUser)) {
                        List<String> alyUserList = Lists.newArrayList();
                        alyUserList.add(busiUser);
                        updateFlowRoleUsers(instanceBid, alyUserList, mSpaceAppData.getSpaceBid(), spaceAppBid, businessInterfacePersonRoleCode);
                    }
                }
            }
            if (mSpaceAppData.containsKey(BUSINESSMANAGER)) {
                Object businessManagerObj = mSpaceAppData.get(BUSINESSMANAGER);
                List<String> businessManagerList = getObjectList(businessManagerObj);
                if (CollectionUtils.isNotEmpty(businessManagerList)) {
                    updateFlowRoleUsers(instanceBid, businessManagerList, mSpaceAppData.getSpaceBid(), spaceAppBid, businessManagerRoleCode);
                }
            }
            if (mSpaceAppData.containsKey(PRODUCTOWNER)) {
                Object businessManagerObj = mSpaceAppData.get(PRODUCTOWNER);
                List<String> businessManagerList = getObjectList(businessManagerObj);
                if (CollectionUtils.isNotEmpty(businessManagerList)) {
                    updateFlowRoleUsers(instanceBid, businessManagerList, mSpaceAppData.getSpaceBid(), spaceAppBid, productmanagerRoleCode);
                }
            }

        }
        if (rrSpaceAppBid.equals(spaceAppBid)) {
            //RR需求特殊流程处理
            if (mSpaceAppData.containsKey(hardwareLmplementationResponsiblePerson)) {
                String hardwareLmplementationResponsiblePersonStr = hardwareLmplementationResponsiblePerson;
                Object hardwareLmplementationResponsiblePerson = mSpaceAppData.get(hardwareLmplementationResponsiblePersonStr);
                if (hardwareLmplementationResponsiblePerson != null) {
                    List<String> hardPersons = getObjectList(hardwareLmplementationResponsiblePerson);
                    updateFlowRoleUsers(instanceBid, hardPersons, mSpaceAppData.getSpaceBid(), spaceAppBid, hardwareresponsiblepersonCode);
                }
            }
            if (mSpaceAppData.containsKey(hardwareInterfacePersonnel)) {
                String hardwareLmplementationResponsiblePersonStr = hardwareInterfacePersonnel;
                Object hardwareLmplementationResponsiblePerson = mSpaceAppData.get(hardwareLmplementationResponsiblePersonStr);
                if (hardwareLmplementationResponsiblePerson != null) {
                    List<String> hardPersons = getObjectList(hardwareLmplementationResponsiblePerson);
                    updateFlowRoleUsers(instanceBid, hardPersons, mSpaceAppData.getSpaceBid(), spaceAppBid, hardwareinterfacepersonnelCode);
                }
            }
        }
        if (specialSpaceAppBidSet.contains(spaceAppBid)) {
            List<ApmRoleUserAO> apmRoleUserAOs = new ArrayList<>();
            if (mSpaceAppData.containsKey("productArea")) {
                Object domainBids = mSpaceAppData.get("productArea");
                if (domainBids != null) {
                    List<String> domainBidList = getObjectList(domainBids);
                    Map<String, String> domainMap = getOtherDomainCodeMap();
                    domainMap.put(domainLeaderCode, productManager);
                    domainMap.put(domainSeCode, domainDevelopmenRepresentative);
                    for (Map.Entry<String, String> entry : domainMap.entrySet()) {
                        //查角色bid
                        ApmRole apmRole = roleService.getByCodeAndApp(entry.getKey(), spaceAppBid);
                        if (apmRole == null) {
                            continue;
                        }
                        //查询领域实例
                        if (CollectionUtils.isEmpty(domainBidList)) {
                            ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                            apmRoleUserAO.setRoleBid(apmRole.getBid());
                            List<ApmUser> userList = new ArrayList<>();
                            apmRoleUserAO.setUserList(userList);
                            apmRoleUserAOs.add(apmRoleUserAO);
                        } else {
                            List<MObject> mObjects = objectModelCrudI.listByBids(domainBidList, domainModelCode);
                            if (CollectionUtils.isNotEmpty(mObjects)) {
                                //查询领域负责人
                                for (MObject mObject : mObjects) {
                                    Object productManagers = mObject.get(entry.getValue());
                                    if (productManagers != null) {
                                        List<String> productManagerList = getObjectList(productManagers);
                                        ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(apmRole.getBid(), null, productManagerList);
                                        apmRoleUserAOs.add(apmRoleUserAO);
                                    }
                                }
                            }
                        }
                    }

                    //updateRoleUser(instanceBid,apmRoleUserAOs);
                }
            }
            if (mSpaceAppData.containsKey("module")) {
                Object modules = mSpaceAppData.get("module");
                if (modules != null) {
                    List<String> moduleList = getObjectList(modules);
                    //查角色bid
                    ApmRole apmRole = roleService.getByCodeAndApp("softwareProductManager", spaceAppBid);
                    if (CollectionUtils.isNotEmpty(moduleList)) {
                        //查询模块实例
                        List<MObject> mObjects = objectModelCrudI.listByBids(moduleList, moudleModelCode);
                        if (CollectionUtils.isNotEmpty(mObjects)) {
                            for (MObject mObject : mObjects) {
                                //查询模块产品负责人
                                Object moudleProductManagers = mObject.get(moudleProductManager);
                                if (moudleProductManagers != null) {
                                    List<String> moudleProductManagerList = getObjectList(moudleProductManagers);
                                    ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(apmRole.getBid(), null, moudleProductManagerList);
                                    apmRoleUserAOs.add(apmRoleUserAO);
                                }
                            }
                        }
                    } else {
                        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                        apmRoleUserAO.setRoleBid(apmRole.getBid());
                        List<ApmUser> userList = new ArrayList<>();
                        apmRoleUserAO.setUserList(userList);
                        apmRoleUserAOs.add(apmRoleUserAO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(apmRoleUserAOs)) {
                ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
                ApmFlowQo apmFlowQo = new ApmFlowQo();
                apmFlowQo.setRoleUserList(apmRoleUserAOs);
                apmFlowQo.setSpaceBid(app.getSpaceBid());
                apmFlowQo.setSpaceAppBid(spaceAppBid);
                updateRoleUser(instanceBid, apmFlowQo);
            }
        }
    }

    @Override
    public Map<String, Object> updateDemandScheduleRoleUserByModuleBid(String instanceBid, String moduleBid) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            throw new PlmBizException("该实例流程节点为空！");
        }
        Map<String, ApmFlowInstanceNode> nodeMap = apmFlowInstanceNodes.stream()
                .collect(Collectors.toMap(ApmFlowInstanceNode::getNodeName, Function.identity(), (a, b) -> b));

        ApmFlowInstanceNode demandScheduleNode = nodeMap.get("需求排期");
        if (Objects.isNull(demandScheduleNode)) {
            throw new PlmBizException("该实例没有需求排期流程节点！");
        }

        MObject module = objectModelCrudI.getByBid(BUS_MODLUE_MODEL_CODE, moduleBid);
        if (Objects.isNull(module)) {
            throw new PlmBizException("模块不存在！");
        }
        Object personResponsible = module.get(PER_ROLE_CODE.getCode());
        if (Objects.isNull(personResponsible)) {
            throw new PlmBizException("该模块不存在责任人！");
        }

        List<String> nodeRoleBids = demandScheduleNode.getNodeRoleBids();
        if (CollectionUtils.isEmpty(nodeRoleBids)) {
            throw new PlmBizException("该没有绑定角色！");
        }
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        List<ApmUser> apmUsers = Lists.newArrayList();
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(nodeRoleBids.get(0));
        ApmUser apmUser = new ApmUser();
        apmUser.setEmpNo(personResponsible.toString());
        apmUsers.add(apmUser);
        apmRoleUserAO.setUserList(apmUsers);
        apmRoleUserAOs.add(apmRoleUserAO);
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setRoleUserList(apmRoleUserAOs);
        apmFlowQo.setSpaceBid(String.valueOf(module.get(SPACE_BID.getCode())));
        apmFlowQo.setSpaceAppBid(String.valueOf(module.get(SPACE_APP_BID.getCode())));
        updateRoleUser(instanceBid, apmFlowQo);
        return module;
    }

    @Override
    public void productOwnerReflectionPdmRoleUser(ApmSpaceApp app, String bid, MSpaceAppData mSpaceAppData) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(bid);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return;
        }
        String flowTemplateBid = apmFlowInstanceNodes.get(0).getFlowTemplateBid();
        if (StringUtils.isNotBlank(flowTemplateBid) && peculiarityFlowTemplateBid.equals(flowTemplateBid)) {
            Object productOwnerObj = mSpaceAppData.get(PRODUCT_OWNER);
            if (Objects.nonNull(productOwnerObj) && StringUtils.isNotBlank(productOwnerObj.toString())) {
                List<ApmRoleVO> roles = roleService.getRoleListBySphereBid(app.getSphereBid());
                Map<String, ApmRoleVO> roleMap = roles.stream().collect(Collectors.toMap(ApmRoleVO::getCode, Function.identity(), (a, b) -> b));
                String apmKey = "pdm";
                if (roleMap.containsKey(apmKey)) {
                    ApmRoleVO apmRoleVO = roleMap.get(apmKey);
                    String productOwnerRoleBid = apmRoleVO.getBid();
                    List<ApmRoleUserAO> roleUserList = Lists.newArrayList();
                    ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                    List<ApmUser> userList = Lists.newArrayList();
                    ApmUser apmUser = new ApmUser();
                    apmUser.setEmpNo(productOwnerObj.toString());
                    userList.add(apmUser);
                    apmRoleUserAO.setRoleBid(productOwnerRoleBid);
                    apmRoleUserAO.setUserList(userList);
                    roleUserList.add(apmRoleUserAO);
                    ApmFlowQo apmFlowQo = new ApmFlowQo();
                    apmFlowQo.setRoleUserList(roleUserList);
                    apmFlowQo.setSpaceAppBid(app.getBid());
                    apmFlowQo.setSpaceBid(app.getSpaceBid());
                    updateRoleUser(bid, apmFlowQo);
                }
            }
        }

    }

    private List<ApmRoleUserAO> getDoainApmRoleUsers(String domainCode, String domainAttribute, Object domainBids, Map<String, String> codeBidMap) {
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        if (domainBids != null) {
            List<String> domainBidList = getObjectList(domainBids);
            //查询领域实例
            List<MObject> mObjects = objectModelCrudI.listByBids(domainBidList, domainModelCode);
            if (CollectionUtils.isNotEmpty(mObjects)) {
                //查询领域负责人  和 领域开发代表
                for (MObject mObject : mObjects) {
                    Object productManagers = mObject.get(domainAttribute);
                    if (productManagers != null) {
                        List<String> productManagerList = getObjectList(productManagers);
                        ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(codeBidMap.get(domainCode), null, productManagerList);
                        apmRoleUserAOs.add(apmRoleUserAO);
                    }
                }
            }
        }
        return apmRoleUserAOs;
    }

    /**
     * 特定流程特定角色人员解析
     *
     * @param instanceBid     实例bid
     * @param domainUserList  领域负责人
     * @param productUserList 产品负责人
     * @param moduleUserList  模块负责人
     * @return
     */
    @Override
    public boolean updateSpecialFlowRoleUsers(String instanceBid, List<String> domainUserList, List<String> productUserList, List<String> moduleUserList, String spaceBid, String spaceAppBid, String rrDomainRoleCode, String rrProductRoleCode) {
        //查角色BID
        ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        if (spaceSphere == null) {
            return false;
        }
        String thisAnalysisRRDomainRoleCode = rrDomainRoleCode;
        List<String> codes = new ArrayList<>();
        if (StringUtils.isEmpty(rrDomainRoleCode)) {
            thisAnalysisRRDomainRoleCode = analysisRRDomainRoleCode;
        }
        codes.add(thisAnalysisRRDomainRoleCode);
        String thisAnalysisRRProductRoleCode = rrProductRoleCode;
        if (StringUtils.isEmpty(rrProductRoleCode)) {
            thisAnalysisRRProductRoleCode = analysisRRProductRoleCode;
        }
        codes.add(thisAnalysisRRProductRoleCode);
        List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(codes, spaceSphere.getBid());
        if (CollectionUtils.isEmpty(apmRoleVOS)) {
            return false;
        }
        Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        //IR的特性角色不需要处理 Domain_Plan_Represent pdm
        List<String> notHandelRoles = Arrays.asList(domainPlanRepresentRoleCode, pdmRoleCode);
        if (!IrModelCode.equals(spaceApp.getModelCode())) {
            ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
            apmRoleUserAO.setRoleBid(codeBidMap.get(thisAnalysisRRDomainRoleCode));
            if (CollectionUtils.isNotEmpty(domainUserList)) {
                List<ApmUser> userList = new ArrayList<>();
                for (String userBid : domainUserList) {
                    ApmUser apmUser = new ApmUser();
                    apmUser.setEmpNo(userBid);
                    userList.add(apmUser);
                }
                apmRoleUserAO.setUserList(userList);
            } else {
                apmRoleUserAO.setUserList(Lists.newArrayList());
            }
            apmRoleUserAOs.add(apmRoleUserAO);
        }

        List<String> allRoleBids = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(moduleUserList)) {
            allRoleBids.addAll(moduleUserList);
        }
        if (!IrModelCode.equals(spaceApp.getModelCode())) {
            ApmRoleUserAO apmRoleUserAO1 = new ApmRoleUserAO();
            apmRoleUserAO1.setRoleBid(codeBidMap.get(thisAnalysisRRProductRoleCode));
            if (CollectionUtils.isNotEmpty(allRoleBids)) {
                List<ApmUser> userList = new ArrayList<>();
                for (String userBid : allRoleBids) {
                    ApmUser apmUser = new ApmUser();
                    apmUser.setEmpNo(userBid);
                    userList.add(apmUser);
                }
                apmRoleUserAO1.setUserList(userList);
            } else {
                apmRoleUserAO1.setUserList(Lists.newArrayList());
            }
            apmRoleUserAOs.add(apmRoleUserAO1);
        }
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setRoleUserList(apmRoleUserAOs);
        apmFlowQo.setSpaceBid(spaceBid);
        apmFlowQo.setSpaceAppBid(spaceAppBid);
        if (CollectionUtils.isNotEmpty(apmRoleUserAOs)) {
            return updateRoleUser(instanceBid, apmFlowQo);
        }
        return true;
    }

    @Override
    public List<ApmRoleUserAO> setApmRoleUserAOs(String instanceBid, String instanceNodeBid, MSpaceAppData mSpaceAppData) {
        List<ApmRoleUserAO> apmRoleUserAOs = Lists.newArrayList();
        ApmFlowInstanceNode apmFlowInstanceNode = apmFlowInstanceNodeService.getByBid(instanceNodeBid);
        if (apmFlowInstanceNode != null && !FlowNodeCompleteType.AUTO_COMPLETE.equals(apmFlowInstanceNode.getComplateType()) && specialFlowTemplateBidSet.contains(apmFlowInstanceNode.getFlowTemplateBid())) {
            //包含特殊处理流程
            List<String> roleBids = apmFlowInstanceNode.getNodeRoleBids();
            //判断是否设置有人员
            List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndRoles(instanceBid, roleBids);
            if (CollectionUtils.isEmpty(apmFlowInstanceRoleUsers)) {
                //没有设置人员，判断是否有特殊角色
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBids(roleBids);
                if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                    List<String> codes = apmRoleVOS.stream().map(ApmRoleVO::getCode).collect(Collectors.toList());
                    Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid, (k1, k2) -> k1));
                    //domainLeader 领域负责人,softwareProductManager 软件产品经理,processInitiator 需求提出人
                    if (codes.contains("processInitiator")) {
                        ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(codeBidMap.get("processInitiator"), mSpaceAppData.getCreatedBy(), null);
                        apmRoleUserAOs.add(apmRoleUserAO);
                    }
                    if (codes.contains(domainLeaderCode)) {
                        //获取领域实例bid
                        Object domainBids = mSpaceAppData.get("productArea");
                        apmRoleUserAOs.addAll(getDoainApmRoleUsers(domainLeaderCode, productManager, domainBids, codeBidMap));
                    }
                    if (codes.contains(domainSeCode)) {
                        //获取领域实例bid
                        Object domainBids = mSpaceAppData.get("productArea");
                        apmRoleUserAOs.addAll(getDoainApmRoleUsers(domainSeCode, domainDevelopmenRepresentative, domainBids, codeBidMap));
                    }
                    if (codes.contains("softwareProductManager")) {
                        //获取领域实例bid
                        Object modules = mSpaceAppData.get("module");
                        if (modules != null) {
                            List<String> moduleList = getObjectList(modules);
                            //查询模块实例
                            List<MObject> mObjects = objectModelCrudI.listByBids(moduleList, moudleModelCode);
                            if (CollectionUtils.isNotEmpty(mObjects)) {
                                for (MObject mObject : mObjects) {
                                    //查询模块产品负责人
                                    Object moudleProductManagers = mObject.get(moudleProductManager);
                                    if (moudleProductManagers != null) {
                                        List<String> moudleProductManagerList = getObjectList(moudleProductManagers);
                                        ApmRoleUserAO apmRoleUserAO = getApmRoleUserAO(codeBidMap.get("softwareProductManager"), null, moudleProductManagerList);
                                        apmRoleUserAOs.add(apmRoleUserAO);
                                    }
                                }

                            }
                        }
                    }
                    //领域其他配置
                    Map<String, String> otherDomainMap = getOtherDomainCodeMap();
                    if (otherDomainMap != null && otherDomainMap.size() > 0) {
                        for (Map.Entry<String, String> entry : otherDomainMap.entrySet()) {
                            if (codes.contains(entry.getKey())) {
                                Object domainBids = mSpaceAppData.get("productArea");
                                apmRoleUserAOs.addAll(getDoainApmRoleUsers(entry.getKey(), entry.getValue(), domainBids, codeBidMap));
                            }
                        }
                    }
                }
                //判断是否为空
                if (CollectionUtils.isNotEmpty(apmRoleUserAOs)) {
                    //找出所有人员
                    List<String> userIds = new ArrayList<>();
                    for (ApmRoleUserAO apmRoleUserAO : apmRoleUserAOs) {
                        userIds.addAll(apmRoleUserAO.getUserList().stream().map(ApmUser::getEmpNo).collect(Collectors.toList()));
                    }
                    if (userIds.contains(SsoHelper.getJobNumber())) {
                        ApmSpaceApp app = apmSpaceAppService.getByBid(apmFlowInstanceNode.getBid());
                        ApmFlowQo apmFlowQo = new ApmFlowQo();
                        apmFlowQo.setSpaceBid(app.getSpaceBid());
                        apmFlowQo.setSpaceAppBid(apmFlowInstanceNode.getSpaceAppBid());
                        apmFlowQo.setRoleUserList(apmRoleUserAOs);
                        updateRoleUser(instanceBid, apmFlowQo);
                    } else {
                        throw new PlmBizException("当前节点没有处理人");
                    }
                } else {
                    throw new PlmBizException("当前节点没有处理人");
                }
            }
        }
        return apmRoleUserAOs;
    }

    private void handlePiSpecialFlow(String spaceBid, String spaceAppBid, MObject mSpaceAppData, List<ApmRoleUserAO> roleUserList) {
        if (piSpecialFlowSpaceAppBid.equals(spaceAppBid)) {
            //解析主管角色
            OpenResponse<UserDTO> openResponse = uacUserService.queryDetail(SsoHelper.getJobNumber());
            UserDTO userDTO = openResponse.getData();
            //当前人主管
            String managerId = userDTO.getManagerId();
            List<String> alyUserList = Lists.newArrayList();
            alyUserList.add(managerId);
            ApmRoleUserAO apmRoleUserManageAO = getApmRoleUserAO(alyUserList, spaceAppBid, managerRoleCode);
            if (apmRoleUserManageAO != null) {
                roleUserList.add(apmRoleUserManageAO);
            }
            //制造中心和财务中心
            if (mSpaceAppData.containsKey(FIST_LEVEL_DEPARTMENT)) {
                //一级部门
                String firstLevelDepartment = mSpaceAppData.get(FIST_LEVEL_DEPARTMENT) + "";
                //制造中心
                if ("ManufacturingCenter".equals(firstLevelDepartment)) {
                    String busiUser = manufacturingCenterBusiUser;
                    if (StringUtils.isNotEmpty(busiUser)) {
                        List<String> alyUserListMf = Lists.newArrayList();
                        alyUserListMf.add(busiUser);
                        ApmRoleUserAO apmRoleUserManageAOMf = getApmRoleUserAO(alyUserListMf, spaceAppBid, businessInterfacePersonRoleCode);
                        if (apmRoleUserManageAOMf != null) {
                            roleUserList.add(apmRoleUserManageAOMf);
                        }
                    }
                } else if ("CustomerServiceCenter".equals(firstLevelDepartment)) {
                    //客户中心
                    String busiUser = customerServiceCenterBusiUser;
                    if (StringUtils.isNotEmpty(busiUser)) {
                        List<String> alyUserListMf = Lists.newArrayList();
                        alyUserListMf.add(busiUser);
                        ApmRoleUserAO apmRoleUserManageAOMf = getApmRoleUserAO(alyUserListMf, spaceAppBid, businessInterfacePersonRoleCode);
                        if (apmRoleUserManageAOMf != null) {
                            roleUserList.add(apmRoleUserManageAOMf);
                        }
                    }
                } else if (mSpaceAppData.containsKey(SECONDARY_DEPARTMENT)) {
                    //二级部门 财务
                    String secondLevelDepartment = mSpaceAppData.get(SECONDARY_DEPARTMENT) + "";
                    Map<String, String> dictItemMap = getDictionaryMap("SECONDARY_DEPARTMENT", LanguageEnum.ZH.getCode());
                    // 判断是否有财务的二级部门
                    if (dictItemMap.containsKey(secondLevelDepartment)) {
                        Map<String, String> secondLevelDepartmentUserMap = getDictionaryMap("PI_FINANCE_DEP_BUSI_USER", LanguageEnum.EN.getCode());
                        String busiUser = secondLevelDepartmentUserMap.get("U_" + secondLevelDepartment);
                        if (StringUtils.isNotEmpty(busiUser)) {
                            List<String> alyUserListFin = Lists.newArrayList();
                            alyUserListFin.add(busiUser);
                            ApmRoleUserAO apmRoleUserManageAOMf = getApmRoleUserAO(alyUserListFin, spaceAppBid, businessInterfacePersonRoleCode);
                            if (apmRoleUserManageAOMf != null) {
                                roleUserList.add(apmRoleUserManageAOMf);
                            }
                        }
                    }
                }
            }
            //业务归属·
            if (mSpaceAppData.containsKey("businessOwnership")) {
                String businessOwnershipNumber = piProductBtConfigService.getProductManager(mSpaceAppData.get("businessOwnership") + "");
                if (StringUtils.isNotEmpty(businessOwnershipNumber)) {
                    List<String> alyUserListFin = Lists.newArrayList();
                    alyUserListFin.add(businessOwnershipNumber);
                    ApmRoleUserAO apmRoleUserManageAOMf = getApmRoleUserAO(alyUserListFin, spaceAppBid, productLineResponsiblePersonRoleCode);
                    if (apmRoleUserManageAOMf != null) {
                        roleUserList.add(apmRoleUserManageAOMf);
                    }
                }
            }
        }
    }

    private void handleRrSpecialFlow(String spaceBid, String spaceAppBid, MObject mSpaceAppData, List<ApmRoleUserAO> roleUserList) {
        //rr需求流程特殊处理
        if (rrSpaceAppBid.equals(spaceAppBid)) {
            if (mSpaceAppData.get(hardwareInterfacePersonnel) != null && StringUtils.isNotEmpty(mSpaceAppData.get(hardwareInterfacePersonnel) + "")) {
                List<String> alyUserListMf = Lists.newArrayList();
                alyUserListMf.add(mSpaceAppData.get(hardwareInterfacePersonnel) + "");
                ApmRoleUserAO apmRoleUserManageAOMf = getApmRoleUserAO(alyUserListMf, spaceAppBid, hardwareinterfacepersonnelCode);
                if (apmRoleUserManageAOMf != null) {
                    roleUserList.add(apmRoleUserManageAOMf);
                }
            }
        }
    }

    /**
     * @Description startProcess
     * @Author jinpeng.bai
     * @Date 2023/10/11 18:10
     * @Param [spaceAppBid, mObject, isEdit]
     * @Return java.lang.Boolean
     * @Since version 1.0
     */
    @Override
    public Boolean startProcess(String spaceBid, String spaceAppBid, MObject mObject, Boolean isEdit) {
        String instanceBid = this.getVersionInstanceBid(spaceAppBid, mObject.getBid());
        if (mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()) != null && StringUtils.isNotEmpty(mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()) + "") &&
                !mObject.get(SpaceAppDataEnum.MOUNT_SPACE_BID.getCode()).toString().equals(mObject.get(SPACE_BID.getCode()).toString())) {
            return true;
        }
        List<CfgViewMetaDto> cfgViewMetaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setSpaceBid(spaceBid);
        apmFlowQo.setSpaceAppBid(spaceAppBid);
        if (com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(cfgViewMetaList)) {
            if (Boolean.TRUE.equals(isEdit)) {
                apmFlowQo.setRoleUserList(Lists.newArrayList());
                return this.updateRoleUser(instanceBid, apmFlowQo);
            } else {
                apmFlowQo.setRoleUserList(Lists.newArrayList());
                return this.startProcess(null, instanceBid, apmFlowQo, mObject);
            }
        }
        String workItemType = Optional.<String>ofNullable(mObject.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) + "").orElse("");
        List<ApmRoleUserAO> roleUserList = Lists.newArrayList();
        // 保存关系组件数据,后面切换成线程池
        Map<String, List<String>> roleList = (Map<String, List<String>>) mObject.get(SpaceAppDataEnum.ROLE_USER.getCode());
        // 特性的流程 产品责任人映射流程节点产品经理角色
        if (StringUtils.isNotBlank(workItemType) && peculiarityFlowTemplateBid.equals(workItemType)) {
            productOwnerReflectionPdmRoleUser(mObject, spaceAppBid, roleList);
        }
        boolean containSpecialFlow = false;
        if (specialFlowTemplateBidSet.contains(workItemType)) {
            containSpecialFlow = true;
        }
        final boolean finalContainSpecialFlow = containSpecialFlow;
        Map<String, String> codeBidMaptemp = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (finalContainSpecialFlow) {
            List<String> roleBids = new ArrayList<>();
            if (roleList != null && !roleList.isEmpty()) {
                roleBids.addAll(roleList.keySet());
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBids(roleBids);
            if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                codeBidMaptemp = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getBid, ApmRoleVO::getCode, (k1, k2) -> k1));
            }
        }
        //处理PI特殊需求流程
        handlePiSpecialFlow(spaceBid, spaceAppBid, mObject, roleUserList);
        //处理软件RR特殊需求流程
        handleRrSpecialFlow(spaceBid, spaceAppBid, mObject, roleUserList);
        Map<String, String> codeBidMap = codeBidMaptemp;
        if (com.transcend.plm.datadriven.common.util.CollectionUtils.isNotEmpty(roleList)) {
            roleList.forEach((key, value) -> {
                if (key.equals(InnerRoleEnum.CREATER.getCode())) {
                    //内置角色处理
                    value = Lists.newArrayList(mObject.getCreatedBy());
                }
                if (com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(value) && !finalContainSpecialFlow) {
                    return;
                }
                List<ApmUser> apmUserList = value.stream().map(userBid -> {
                    ApmUser apmUser = new ApmUser();
                    apmUser.setEmpNo(userBid);
                    return apmUser;
                }).collect(Collectors.toList());
                ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                apmRoleUserAO.setRoleBid(key);
                if (finalContainSpecialFlow) {
                    String code = codeBidMap.get(key);
                    //领域角色
                    Map<String, String> otherDomainMap = getOtherDomainCodeMap();
                    otherDomainMap.put(domainLeaderCode, productManager);
                    otherDomainMap.put(domainSeCode, domainDevelopmenRepresentative);

                    //domainLeader 领域负责人,softwareProductManager 软件产品经理,processInitiator 需求提出人
                    if (otherDomainMap.containsKey(code)) {
                        Object domainBids = mObject.get("productArea");
                        if (domainBids != null) {
                            List<String> domainBidList = getObjectList(domainBids);
                            //查询领域实例
                            List<MObject> mObjects = objectModelCrudI.listByBids(domainBidList, domainModelCode);
                            if (CollectionUtils.isNotEmpty(mObjects)) {
                                //查询领域负责人
                                for (MObject mObject1 : mObjects) {
                                    Object productManagers = mObject1.get(otherDomainMap.get(code));
                                    if (productManagers != null) {
                                        List<String> productManagerList = getObjectList(productManagers);
                                        if (CollectionUtils.isNotEmpty(productManagerList)) {
                                            if (CollectionUtils.isEmpty(apmUserList)) {
                                                apmUserList = new ArrayList<>();
                                            }
                                            for (String userBid : productManagerList) {
                                                ApmUser apmUser = new ApmUser();
                                                apmUser.setEmpNo(userBid);
                                                apmUserList.add(apmUser);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if ("softwareProductManager".equals(code)) {
                        //先取产品负责人productOwner
                        Object productOwner = mObject.get("productOwner");
                        if (productOwner != null) {
                            List<String> moudleProductManagerList = getObjectList(productOwner);
                            if (CollectionUtils.isNotEmpty(moudleProductManagerList)) {
                                if (CollectionUtils.isEmpty(apmUserList)) {
                                    apmUserList = new ArrayList<>();
                                }
                                for (String userBid : moudleProductManagerList) {
                                    ApmUser apmUser = new ApmUser();
                                    apmUser.setEmpNo(userBid);
                                    apmUserList.add(apmUser);
                                }
                            }
                        } else {
                            //MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(apmFlowInstanceNode.getSpaceAppBid(), apmFlowInstanceNode.getInstanceBid());
                            //获取领域实例bid
                            Object modules = mObject.get("module");
                            if (modules != null) {
                                List<String> moduleList = getObjectList(modules);
                                //查询模块实例
                                List<MObject> mObjects = objectModelCrudI.listByBids(moduleList, moudleModelCode);
                                if (CollectionUtils.isNotEmpty(mObjects)) {
                                    for (MObject mObject1 : mObjects) {
                                        //查询模块产品负责人
                                        Object moudleProductManagers = mObject1.get(moudleProductManager);
                                        if (moudleProductManagers != null) {
                                            List<String> moudleProductManagerList = getObjectList(moudleProductManagers);
                                            if (CollectionUtils.isNotEmpty(moudleProductManagerList)) {
                                                if (CollectionUtils.isEmpty(apmUserList)) {
                                                    apmUserList = new ArrayList<>();
                                                }
                                                for (String userBid : moudleProductManagerList) {
                                                    ApmUser apmUser = new ApmUser();
                                                    apmUser.setEmpNo(userBid);
                                                    apmUserList.add(apmUser);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if ("processInitiator".equals(code)) {
                        //这里是实例创建者
                        if (CollectionUtils.isEmpty(apmUserList)) {
                            apmUserList = new ArrayList<>();
                        }
                        ApmUser apmUser = new ApmUser();
                        apmUser.setEmpNo(mObject.getCreatedBy());
                        apmUserList.add(apmUser);
                    } else if (pollingCode.equals(code)) {
                        //角色只取第一个
                        if (CollectionUtils.isNotEmpty(apmUserList)) {
                            ApmUser apmUser = apmUserList.get(0);
                            apmUserList = new ArrayList<>();
                            apmUserList.add(apmUser);
                        }
                    }
                }
                apmRoleUserAO.setUserList(apmUserList);
                roleUserList.add(apmRoleUserAO);
            });
        }


        if (Boolean.TRUE.equals(isEdit)) {
            apmFlowQo.setRoleUserList(roleUserList);
            return this.updateRoleUser(instanceBid, apmFlowQo);
        } else {
            apmFlowQo.setRoleUserList(roleUserList);
            return this.startProcess(workItemType, instanceBid, apmFlowQo, mObject);
        }
    }

    private void productOwnerReflectionPdmRoleUser(MObject mObject, String spaceAppBid, Map<String, List<String>> roleList) {
        if (com.transcend.plm.datadriven.common.util.CollectionUtils.isEmpty(roleList)) {
            return;
        }
        String productOwner = Optional.<String>ofNullable(mObject.get(PRODUCT_OWNER) + "").orElse("");
        //获取空间应用数据
        ApmSpaceApp app = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        List<ApmRoleVO> roles = roleService.getRoleListBySphereBid(app.getSphereBid());
        Map<String, ApmRoleVO> roleMap = roles.stream().collect(Collectors.toMap(ApmRoleVO::getCode, Function.identity(), (a, b) -> b));
        String apmKey = "pdm";
        if (roleMap.containsKey(apmKey)) {
            ApmRoleVO apmRoleVO = roleMap.get(apmKey);
            String productOwnerRoleBid = apmRoleVO.getBid();
            if (roleList.containsKey(productOwnerRoleBid)) {
                roleList.replace(productOwnerRoleBid, Lists.newArrayList(productOwner));
            }
        }

    }



}
