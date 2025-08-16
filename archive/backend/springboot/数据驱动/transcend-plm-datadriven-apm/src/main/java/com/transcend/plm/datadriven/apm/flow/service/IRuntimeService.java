package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowInstanceProcessDto;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceNodeVO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceProcessVo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 运行时Service
 * @createTime 2023-10-07 10:25:00
 */
public interface IRuntimeService {

    /**
     * 定义一个发起流程的方法
     *
     * @param templateBid templateBid
     * @param instanceBid instanceBid
     * @param qo          qo
     * @param mObject     mObject
     * @return {@link Boolean}
     */
    Boolean startProcess(String templateBid, String instanceBid, ApmFlowQo qo, MObject mObject);


    /**
     * 定义一个查询流程状态的方法
     *
     * @param instanceBid instanceBid
     * @return {@link ApmFlowInstanceVO}
     */
    ApmFlowInstanceVO listInstanceNodes(String instanceBid);


    /**
     * 查询流程状态 包含默认的状态流程
     * listInstanceNodesByApmStateQo
     *
     * @param apmStateQo apmStateQo
     * @return {@link ApmFlowInstanceVO}
     */
    ApmFlowInstanceVO listInstanceNodesByApmStateQo(ApmStateQo apmStateQo);


    /**
     * 定义一个完成节点任务的方法
     * completeNode
     *
     * @param nodeBid  nodeBid
     * @param pageData pageData
     * @return {@link Boolean}
     */
    List<ApmFlowInstanceNode> completeNodeReturnNextNode(String nodeBid, MSpaceAppData pageData);


    /**
     * 定义一个完成节点任务的方法
     * completeNode
     *
     * @param nodeBid  nodeBid
     * @param pageData pageData
     * @return {@link Boolean}
     */
    Boolean completeNode(String nodeBid, MSpaceAppData pageData);

    /**
     * completeNodeCheck
     *
     * @param app         app
     * @param currentNode currentNode
     * @param mObject     mObject
     */
    void completeNodeCheck(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject);

    /**
     * runStartNode
     *
     * @param instanceBid instanceBid
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     */
    void runStartNode(String instanceBid, String spaceBid, String spaceAppBid);

    /**
     * runStartNodeGetLifeCycleCode
     *
     * @param instanceBid instanceBid
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param pageData    pageData
     * @return {@link String}
     */
    String runStartNodeGetLifeCycleCode(String instanceBid, String spaceBid, String spaceAppBid, MSpaceAppData pageData);

    /**
     * completeNodeForce
     *
     * @param nodeBid nodeBid
     * @return {@link Boolean}
     */
    Boolean completeNodeForce(String nodeBid);

    /**
     * rollback
     *
     * @param nodeBid  nodeBid
     * @param runEvent runEvent
     * @return {@link Boolean}
     */
    Boolean rollback(String nodeBid, boolean runEvent);

    /**
     * listNodeRoleUser
     *
     * @param nodeBid nodeBid
     * @return {@link List<ApmRoleUserAO>}
     */
    List<ApmRoleUserAO> listNodeRoleUser(String nodeBid);

    /**
     * listLifeCycleCodeRoleUser
     *
     * @param lifeCycleCode lifeCycleCode
     * @param instanceBid   instanceBid
     * @return {@link List<ApmRoleUserAO>}
     */
    List<ApmRoleUserAO> listLifeCycleCodeRoleUser(String lifeCycleCode, String instanceBid);

    /**
     * listInstanceRoleUser
     *
     * @param instanceBid instanceBid
     * @return {@link List<ApmRoleUserAO>}
     */
    List<ApmRoleUserAO> listInstanceRoleUser(String instanceBid);


    /**
     * 定义一个方法，更新实例的角色人员
     * updateRoleUser
     *
     * @param instanceBid instanceBid
     * @param qo          qo
     * @return {@link Boolean}
     */
    Boolean updateRoleUser(String instanceBid, ApmFlowQo qo);

    /**
     * updateTonesRoleUser
     *
     * @param instanceBid instanceBid
     * @param qo          qo
     * @return {@link Boolean}
     */
    Boolean updateTonesRoleUser(String instanceBid, ApmFlowQo qo);

    /**
     * updateSpecialFlowRoleUsers
     *
     * @param instanceBid       instanceBid
     * @param domainUserList    domainUserList
     * @param productUserList   productUserList
     * @param moduleUserList    moduleUserList
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param rrDomainRoleCode  rrDomainRoleCode
     * @param rrProductRoleCode rrProductRoleCode
     * @return {@link boolean}
     */

    boolean updateSpecialFlowRoleUsers(String instanceBid, List<String> domainUserList, List<String> productUserList, List<String> moduleUserList, String spaceBid, String spaceAppBid, String rrDomainRoleCode, String rrProductRoleCode);

    /**
     * setApmRoleUserAOs
     *
     * @param instanceBid     instanceBid
     * @param instanceNodeBid instanceNodeBid
     * @param mSpaceAppData   mSpaceAppData
     * @return {@link List<ApmRoleUserAO>}
     */
    List<ApmRoleUserAO> setApmRoleUserAOs(String instanceBid, String instanceNodeBid, MSpaceAppData mSpaceAppData);

    /**
     * updateRoleUserByLifeCycleCode
     *
     * @param instanceBid   instanceBid
     * @param lifeCycleCode lifeCycleCode
     * @param roleUserList  roleUserList
     * @return {@link Boolean}
     */
    Boolean updateRoleUserByLifeCycleCode(String instanceBid, String lifeCycleCode, List<ApmRoleUserAO> roleUserList);


    /**
     * 定义一个方法，根据实例bid删除流程
     *
     * @param instanceBid instanceBid
     * @return {@link Boolean}
     */
    Boolean deleteProcess(String instanceBid);


    /**
     * 定义一个方法，批量删除流程
     * deleteProcess
     *
     * @param instanceBids instanceBids
     * @return {@link Boolean}
     */
    Boolean deleteProcess(List<String> instanceBids);

    /**
     * 查询流程实例的历程  TODO
     *
     * @param instanceBid 实例bid
     * @return List<ApmFlowInstanceProcessVo>
     */
    List<ApmFlowInstanceProcessVo> listInstanceProcess(String instanceBid);

    /**
     * 记录流程实例的历程
     *
     * @param dto 流程实例历程dto
     * @return Boolean
     */
    Boolean saveFlowProcess(ApmFlowInstanceProcessDto dto);

    /**
     * saveFlowProcess
     *
     * @param nodeBid     nodeBid
     * @param instanceBid instanceBid
     * @param action      action
     * @param content     content
     * @return {@link Boolean}
     */
    Boolean saveFlowProcess(String nodeBid, String instanceBid, String action, Map<String, Object> content);

    /**
     * saveFlowProcess
     *
     * @param nodeBid     nodeBid
     * @param instanceBid instanceBid
     * @param action      action
     * @param apmFlowInstanceNode      apmFlowInstanceNode
     * @param content     content
     * @return {@link Boolean}
     */
    Boolean saveFlowProcess(String nodeBid, String instanceBid, String action, ApmFlowInstanceNode apmFlowInstanceNode, Map<String, Object> content);

    /**
     * updateFlowRoleUsers
     *
     * @param instanceBid instanceBid
     * @param alyUserList alyUserList
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return {@link boolean}
     */
    boolean updateFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code);

    /**
     * updateFlowRoleUsers
     *
     * @param instanceBid instanceBid
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param roleUserMap        roleUserMap
     * @return {@link boolean}
     */
    boolean updateFlowRoleUsers(String instanceBid, String spaceBid, String spaceAppBid, Map<String, List<String>> roleUserMap);
    /**
     * updateFlowRoleUsers
     *
     * @param instanceBid instanceBid
     * @param alyUserList alyUserList
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return {@link boolean}
     */
    boolean saveOrupdateFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code);

    boolean saveFlowRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code);
    /**
     * updateFlowInnerRoleUsers
     *
     * @param instanceBid instanceBid
     * @param alyUserList alyUserList
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return {@link boolean}
     */
    boolean updateFlowInnerRoleUsers(String instanceBid, List<String> alyUserList, String spaceBid, String spaceAppBid, String code);

    /**
     * getVersionInstanceBid
     *
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link String}
     */
    String getVersionInstanceBid(String spaceAppBid, String instanceBid);

    /**
     * startProcess
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param mObject     mObject
     * @param isEdit      isEdit
     * @return {@link Boolean}
     */
    Boolean startProcess(String spaceBid, String spaceAppBid, MObject mObject, Boolean isEdit);

    /**
     * setSpecialApmRoleUsers
     *
     * @param spaceAppBid   spaceAppBid
     * @param instanceBid   instanceBid
     * @param mSpaceAppData mSpaceAppData
     */
    void setSpecialApmRoleUsers(String spaceAppBid, String instanceBid, MSpaceAppData mSpaceAppData);

    /**
     * updateDemandScheduleRoleUserByModuleBid
     *
     * @param instanceBid instanceBid
     * @param moduleBid   moduleBid
     * @return {@link Map<String, Object>}
     */
    Map<String, Object> updateDemandScheduleRoleUserByModuleBid(String instanceBid, String moduleBid);

    /**
     * productOwnerReflectionPdmRoleUser
     *
     * @param app           app
     * @param bid           bid
     * @param mSpaceAppData mSpaceAppData
     */
    void productOwnerReflectionPdmRoleUser(ApmSpaceApp app, String bid, MSpaceAppData mSpaceAppData);

    /**
     * createOlnyTask
     *
     * @param apmFlowInstanceNode apmFlowInstanceNode
     */
    void createOlnyTask(ApmFlowInstanceNode apmFlowInstanceNode);

    /**
     * createOlnyTask
     *
     * @param spaceBid spaceBid
     * @param spaceAppBid spaceAppBid
     * @param mObject mObject
     */
    void startStateFlow(String spaceBid, String spaceAppBid, MObject mObject);

    /**
     * 查看关键节点
     *
     * @param apmStateQo apmStateQo
     * @return
     */
    List<ApmFlowTemplateNode> listKeyLifeCycleCodes(ApmStateQo apmStateQo);


    /**
     * 查看可回退的节点 集合
     * @param instanceBid instanceBid
     * @return List<ApmFlowInstanceNodeVO>
     */
    List<ApmFlowInstanceNodeVO> listRollbackNode(String instanceBid);

    /**
     * 复制流程节点状态
     * @param sourceInstanceBid sourceInstanceBid
     * @param targetInstanceBids targetInstanceBids
     * @return Boolean
     */
    Boolean copyNodeState(String modelCode, String sourceInstanceBid, List<String> targetInstanceBids);

    /**
     * 流程节点到达发送飞书通知
     * @param apmFlowInstanceNode apmFlowInstanceNode
     * @param empNos empNos
     * @return Boolean
     */
    void sendFeishu(ApmFlowInstanceNode apmFlowInstanceNode, List<String> empNos);

    /**
     * 流程节点到达发送飞书通知
     * @param apmFlowInstanceNode apmFlowInstanceNode
     * @param empNos empNos
     * @return Boolean
     */
    void sendFeishu(ApmFlowInstanceNode apmFlowInstanceNode, List<String> empNos, String instanceName);
}
