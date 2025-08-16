package com.transcend.plm.datadriven.apm.flow.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmFlowInstanceRoleUser
 */
@Mapper
public interface ApmFlowInstanceRoleUserMapper extends BaseMapper<ApmFlowInstanceRoleUser> {

    /**
     * 删除指定实例Bid和角色Bid的流程实例角色用户记录。
     *
     * @param instanceBid 要删除的实例Bid
     * @param updatedRoleBids    要删除的角色Bid列表
     * @return 删除操作是否成功
     */
    boolean deleteByInstanceBidAndRoleBids(@Param("instanceBid") String instanceBid, @Param("roleBids") List<String> updatedRoleBids);

    /**
     * 删除指定的记录。
     *
     * @param ids 要删除的记录的ID列表
     * @return 删除操作是否成功，如果成功则返回true，否则返回false
     */
    boolean deleteByIds(@Param("ids") List<Integer> ids);


    /**
     * 删除指定实例Bid和生命周期编码的流程实例角色用户记录。
     *
     * @param instanceBid 要删除的实例Bid
     * @param lifeCycleCode 生命周期编码
     * @param updatedRoleBids 要删除的角色Bid列表
     * @return 删除操作是否成功
     */
    boolean deleteByInstanceBidAndLifeCycleCode(@Param("instanceBid") String instanceBid, @Param("lifeCycleCode") String lifeCycleCode, @Param("roleBids") List<String> updatedRoleBids);


    /**
     * 删除指定实例Bid的流程实例角色用户记录。
     *
     * @param instanceBids 要删除的实例Bid列表
     * @return 删除操作是否成功，如果成功则返回true，否则返回false
     */
    boolean deleteByInstanceBids(List<String> instanceBids);

    /**
     * 查询指定实例Bid的流程实例角色用户记录。
     *
     * @param instanceBids 要查询的实例Bid列表
     * @return 符合条件的流程实例角色用户记录列表
     */
    List<ApmFlowInstanceRoleUser> queryNodeUsersByInstanceBids(List<String> instanceBids);


}




