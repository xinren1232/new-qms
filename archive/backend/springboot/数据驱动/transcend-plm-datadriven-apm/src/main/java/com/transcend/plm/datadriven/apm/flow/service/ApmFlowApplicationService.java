package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowTemplateAO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceVO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowNodeDirectionVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDisplayCondition;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateNodeVO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateVO;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author unknown
 */
public interface ApmFlowApplicationService {

    /**
     *
     * 获取ApmFlowNodeDisplayCondition列表
     *
     * @param nodeBids 节点bid列表
     * @return ApmFlowNodeDisplayCondition对象列表
     */
    List<ApmFlowNodeDisplayCondition> getApmFlowNodeDisplayConditions(List<String> nodeBids);

    /**
     * 根据节点bid查询节点的连接方向列表。
     *
     * @param nodeBid 节点bid
     * @return 节点的连接方向列表
     */
    List<ApmFlowNodeDirectionVO> listDirectionVOByNodeBid(String nodeBid);
    /**
     * 保存或更新流程模板。
     *
     * @param apmFlowTemplateAO 流程模板对象
     * @return 保存或更新后的流程模板对象
     */
    ApmFlowTemplateVO saveOrUpdate(ApmFlowTemplateAO apmFlowTemplateAO);

    /**
     *
     * 返回当前模板的所有节点列表。
     *
     * @param templateBid 模板的业务id
     * @return 包含ApmFlowTemplateNodeVO对象的列表，表示当前模板的所有节点
     */
    List<ApmFlowTemplateNodeVO> listCurrentTemplateNodes(String templateBid);


    /**
     * 根据模板bid获取模板布局。
     *
     * @param templateBid 模板bid
     * @return 模板布局
     */
    String getTemplateLayout(String templateBid);

    /**
     * 根据业务id获取ApmFlowTemplateVO对象。
     *
     * @param bid 业务id
     * @return ApmFlowTemplateVO对象
     */
    ApmFlowTemplateVO getByBid(String bid);

    /**
     * 根据空间应用的业务ID(spaceAppBid)查询与之关联的流程模板列表。
     *
     * @param spaceAppBid 空间应用的业务ID
     * @return 与空间应用关联的流程模板对象列表
     */
    List<ApmFlowTemplateVO> listBySpaceAppBid(String spaceAppBid);

    /**
     * 根据节点bid查询节点的详细信息。
     *
     * @param nodeBid 节点的业务ID
     * @return 包含节点详细信息的ApmFlowTemplateNodeVO对象
     */
    ApmFlowTemplateNodeVO getNodeDetail(String nodeBid);

    /**
     * 查询节点事件列表。
     *
     * @param eventClassification 事件分类
     * @param nodeBid 节点业务ID
     * @param version 版本
     * @return 符合条件的节点事件列表
     */
    List<ApmFlowNodeEvent> listNodeEvents(int eventClassification, String nodeBid, String version);

    /**
     * 查询流程模板的角色和身份信息列表。
     *
     * @param flowTemplateBid 流程模板业务ID
     * @return 包含角色和身份信息的列表
     */
    List<ApmRoleAndIdentityVo> listFlowTemplateRoles(String flowTemplateBid);

    /**
     * 删除指定的模板。
     *
     * @param templateBid 模板的业务id
     * @return 如果删除成功则返回true，否则返回false
     */
    boolean delete(String templateBid);

    /**
     * 根据空间业务ID和模型编码查询流程模板列表。
     *
     * @param spaceBid 空间业务ID
     * @param modelCode 模型编码
     * @return 包含流程模板对象的列表
     */
    List<ApmFlowTemplateVO> listBySpaceBidAndModelCode(String spaceBid, String modelCode);

    /**
     * 复制流程。
     *
     * @param appBidMap 应用的业务ID映射，其中键是源应用的业务ID，值是目标应用的业务ID
     * @param roleBidMap 角色的业务ID映射，其中键是源角色的业务ID，值是目标角色的业务ID
     * @return 复制流程是否成功
     */
    boolean copyFlow(Map<String,String> appBidMap, Map<String,String> roleBidMap);

    /**
     * 根据模板业务ID列出所有符合条件的节点列表。
     *
     * @param templateBid 模板业务ID
     * @return 符合条件的节点列表，列表中包含ApmFlowTemplateNodeVO对象，表示当前模板的所有节点
     */
    List<ApmFlowTemplateNodeVO> listNodesByTemplateBid(String templateBid);

    /**
     * 获取给定空间应用的生命周期状态。
     *
     * @param spaceAppBid 空间应用的业务ID
     * @return 返回ApmLifeCycleStateVO对象，表示给定空间应用的生命周期状态
     */
    ApmLifeCycleStateVO getLifeCycleState(String spaceAppBid);

    /**
     * 返回给定ApmStateQo对象的下一个状态列表。
     *
     * @param apmStateQo ApmStateQo对象，包含查询条件
     * @return 包含ApmStateVO对象的列表，表示给定ApmStateQo对象的下一个状态列表
     */
    List<ApmStateVO> listNextStates(ApmStateQo apmStateQo);

    /**
     * 根据ApmStateQo对象查询流程实例节点列表。
     *
     * @param apmStateQo ApmStateQo对象，包含查询条件
     * @return ApmFlowInstanceVO对象，表示流程实例节点列表
     */
    ApmFlowInstanceVO listInstanceNodesByApmStateQo(ApmStateQo apmStateQo);

    /**
     * 根据实例业务ID列表查询节点用户。
     *
     * @param instanceBids 实例业务ID列表
     * @return 以节点bid为键，对应节点上的用户集合为值的映射表
     */
    Map<String, Set<String>> queryNodeUsersByInstanceBids(List<String> instanceBids);
}
