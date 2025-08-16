package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowInstanceRoleUserService extends IService<ApmFlowInstanceRoleUser> {

    /**
     * 根据流程实例bid和角色bid查询
     * @param instanceBid 流程实例bid
     * @param nodeRoleBids 角色bid集合
     * @return 角色用户关系集合
     */
    List<ApmFlowInstanceRoleUser> listByInstanceBidAndRoles(String instanceBid, List<String> nodeRoleBids);

    /**
     * 根据流程实例bid和用户编号查询流程实例角色人员关系列表。
     *
     * @param instanceBid 流程实例bid
     * @param userNo 用户编号
     * @param spaceAppBid 空间应用bid
     * @return 流程实例角色人员关系列表
     */
    List<ApmFlowInstanceRoleUser> listByInstanceBidAndUserNo(String instanceBid, String userNo,String spaceAppBid);


    /**
     * 根据流程实例bid和生命周期编码查询流程实例角色人员关系列表。
     *
     * @param instanceBid 流程实例bid
     * @param lifeCycleCode 生命周期编码
     * @return 流程实例角色人员关系列表
     */
    List<ApmFlowInstanceRoleUser> listByInstanceBidAndLifeCycleCode(String instanceBid,String lifeCycleCode);

    /**
     * 根据流程实例bid和生命周期编码查询流程实例角色人员关系列表。
     *
     * @param instanceBid 流程实例bid
     * @param lifeCycleCodes 生命周期编码列表
     * @return 流程实例角色人员关系列表
     */
    List<ApmFlowInstanceRoleUser> listByInstanceBidAndLifeCycleCodes(String instanceBid, List<String> lifeCycleCodes);

    /**
     *  根据流程实例bid和角色bid删除
     * @param instanceBid 流程实例bid
     * @param updatedRoleBids 角色bid集合
     * @return 是否删除成功
     */
    boolean deleteByInstanceBidAndRoleBids(String instanceBid, List<String> updatedRoleBids);

    /**
     * 根据流程实例bid和角色bid集合删除流程实例角色人员关系。
     *
     * @param instanceBid 流程实例bid
     * @param updatedRoleBids 角色bid集合
     * @return 是否删除成功
     */
    boolean deleteByInstanceBidAndRoleBidsToIds(String instanceBid, List<String> updatedRoleBids);

    /**
     * 根据流程实例bid和生命周期编码删除流程实例角色人员关系。
     *
     * @param instanceBid 流程实例bid
     * @param lifeCycleCode 生命周期编码
     * @param updatedRoleBids 角色bid集合
     * @return 是否删除成功
     */
    boolean deleteByInstanceBidAndLifeCycleCode(String instanceBid,String lifeCycleCode, List<String> updatedRoleBids);

    /**
     * 根据流程实例bid查询
     * @param instanceBid 流程实例bid
     * @return 角色用户关系集合
     */
    List<ApmFlowInstanceRoleUser> listByInstanceBid(String instanceBid);

    /**
     * 通过实例bid删除流程实例角色人员关系。
     *
     * @param instanceBid 流程实例bid
     * @return 删除是否成功的布尔值
     */
    boolean deleteByInstanceBid(String instanceBid);

    /**
     * 根据流程实例的bid列表删除流程实例角色人员关系。
     *
     * @param instanceBids 流程实例的bid列表
     * @return 删除是否成功的布尔值
     */
    boolean deleteByInstanceBids(List<String> instanceBids);

    /**
     * 根据流程实例的bid列表查询节点用户信息。
     *
     * @param instanceBids 流程实例的bid列表，不能为空。
     * @return 节点用户信息列表。
     */
    List<ApmFlowInstanceRoleUser> queryNodeUsersByInstanceBids(List<String> instanceBids);
}
