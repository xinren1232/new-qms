package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.plm.datadriven.apm.flow.pojo.event.ApmFlowInstanceRoleUserChangeEvent;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowInstanceRoleUserMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
public class ApmFlowInstanceRoleUserServiceImpl extends ServiceImpl<ApmFlowInstanceRoleUserMapper, ApmFlowInstanceRoleUser>
    implements ApmFlowInstanceRoleUserService {

    @Resource
    private ApmFlowInstanceRoleUserMapper apmFlowInstanceRoleUserMapper;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<ApmFlowInstanceRoleUser> listByInstanceBidAndRoles(String instanceBid, List<String> nodeRoleBids) {
        if (nodeRoleBids == null || CollectionUtils.isEmpty(nodeRoleBids)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid)
            .in(ApmFlowInstanceRoleUser::getRoleBid, nodeRoleBids));
    }

    /**
     *  查询流程用户角色
     * @param instanceBid 流程实例id
     * @param userNo 用户id
     * @return
     */
    public List<ApmFlowInstanceRoleUser> listByInstanceBidAndUserNo(String instanceBid, String userNo,String spaceAppBid) {
        return this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid)
                .eq(ApmFlowInstanceRoleUser::getUserNo, userNo).eq(StringUtil.isNotBlank(spaceAppBid),ApmFlowInstanceRoleUser::getSpaceAppBid,spaceAppBid).eq(ApmFlowInstanceRoleUser::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED));
    }

    @Override
    public List<ApmFlowInstanceRoleUser> listByInstanceBidAndLifeCycleCode(String instanceBid,String lifeCycleCode){
        if (StringUtil.isBlank(instanceBid) || StringUtil.isBlank(lifeCycleCode)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid).eq(ApmFlowInstanceRoleUser::getLifeCycleCode,lifeCycleCode));
    }

    @Override
    public List<ApmFlowInstanceRoleUser> listByInstanceBidAndLifeCycleCodes(String instanceBid, List<String> lifeCycleCodes){
        if (lifeCycleCodes == null || CollectionUtils.isEmpty(lifeCycleCodes)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid)
                .in(ApmFlowInstanceRoleUser::getLifeCycleCode, lifeCycleCodes));
    }

    @Override
    public List<ApmFlowInstanceRoleUser> listByInstanceBid(String instanceBid) {
        Assert.hasText(instanceBid, "示例Bid不能为空");
        return this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid));
    }

    @Override
    public boolean deleteByInstanceBidAndRoleBids(String instanceBid, List<String> updatedRoleBids) {

        if (updatedRoleBids == null || CollectionUtils.isEmpty(updatedRoleBids)) {
            return false;
        }
        return apmFlowInstanceRoleUserMapper.deleteByInstanceBidAndRoleBids(instanceBid, updatedRoleBids);
    }

    @Override
    public boolean deleteByInstanceBidAndRoleBidsToIds(String instanceBid, List<String> updatedRoleBids) {
        List<Integer> ids = new ArrayList<>();
        if (updatedRoleBids == null || CollectionUtils.isEmpty(updatedRoleBids)) {
            return false;
        }
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = this.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().eq(ApmFlowInstanceRoleUser::getInstanceBid, instanceBid)
                .in(ApmFlowInstanceRoleUser::getRoleBid, updatedRoleBids));
        if(CollectionUtils.isEmpty(apmFlowInstanceRoleUsers)){
            return false;
        }
        ids = apmFlowInstanceRoleUsers.stream().map(ApmFlowInstanceRoleUser::getId).collect(Collectors.toList());
        return apmFlowInstanceRoleUserMapper.deleteByIds(ids);
    }

    @Override
    public boolean deleteByInstanceBidAndLifeCycleCode(String instanceBid,String lifeCycleCode, List<String> updatedRoleBids){
        if (updatedRoleBids == null || CollectionUtils.isEmpty(updatedRoleBids) || StringUtils.isEmpty(lifeCycleCode)) {
            return false;
        }
        return apmFlowInstanceRoleUserMapper.deleteByInstanceBidAndLifeCycleCode(instanceBid,lifeCycleCode, updatedRoleBids);
    }

    @Override
    public boolean deleteByInstanceBid(String instanceBid) {
        Assert.hasText(instanceBid, "实例Bid不能为空");
        return apmFlowInstanceRoleUserMapper.deleteByInstanceBidAndRoleBids(instanceBid, null);

    }

    @Override
    public boolean deleteByInstanceBids(List<String> instanceBids) {
        if (CollectionUtils.isEmpty(instanceBids)) {
            return false;
        }
        return apmFlowInstanceRoleUserMapper.deleteByInstanceBids(instanceBids);
    }

    @Override
    public List<ApmFlowInstanceRoleUser> queryNodeUsersByInstanceBids(List<String> instanceBids) {
        Assert.notEmpty(instanceBids, "实例Bids不能为空");
        return this.baseMapper.queryNodeUsersByInstanceBids(instanceBids);
    }

    @Override
    public boolean saveBatch(Collection<ApmFlowInstanceRoleUser> entityList) {
        applicationEventPublisher.publishEvent(new ApmFlowInstanceRoleUserChangeEvent(entityList));
        return super.saveBatch(entityList);
    }
}




