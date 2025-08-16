package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppCustomViewRoleMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewRolePo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppCustomViewRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jie.luo1
 * @description ApmSpaceAppCustomView
 * @createDate 2023-09-20 16:15:29
 */
@Service
public class ApmSpaceAppCustomViewRoleServiceImpl extends ServiceImpl<ApmSpaceAppCustomViewRoleMapper, ApmSpaceAppCustomViewRolePo>
        implements ApmSpaceAppCustomViewRoleService {


    /**
     * @param customViewBid
     * @return
     */
    @Override
    public List<String> listRoleBidByCustomViewBid(String customViewBid) {
        List<ApmSpaceAppCustomViewRolePo> list = list(Wrappers.<ApmSpaceAppCustomViewRolePo>lambdaQuery()
                .eq(ApmSpaceAppCustomViewRolePo::getCustomViewBid, customViewBid));
        return Optional.ofNullable(list).orElse(Lists.newArrayList())
                .stream().map(ApmSpaceAppCustomViewRolePo::getRoleBid).collect(Collectors.toList());
    }

    /**
     * @param customViewBid
     * @return
     */


    /**
     * @param spaceAppBid
     * @param bid
     * @param roleBids
     * @return
     */
    @Override
    public boolean batchAdd(String spaceAppBid, String bid, List<String> roleBids) {
        return this.saveBatch(roleBids.stream().map(roleBid ->
                ApmSpaceAppCustomViewRolePo.of()
                        .setSpaceAppBid(spaceAppBid)
                        .setRoleBid(roleBid)
                        .setCustomViewBid(bid)
        ).collect(Collectors.toList()));
    }

    /**
     * @param bid
     * @param deleteRoleBids
     * @return
     */
    @Override
    public boolean remove(String bid, List<String> deleteRoleBids) {
        return remove(new QueryWrapper<ApmSpaceAppCustomViewRolePo>().lambda().in(ApmSpaceAppCustomViewRolePo::getRoleBid, deleteRoleBids));
    }

    /**
     * @param roleBids
     * @return
     */
    @Override
    public List<String> listCustomViewBidByRoleBids(List<String> roleBids) {
        List<ApmSpaceAppCustomViewRolePo> list = list(Wrappers.<ApmSpaceAppCustomViewRolePo>lambdaQuery()
                .in(ApmSpaceAppCustomViewRolePo::getRoleBid, roleBids));
        return Optional.ofNullable(list).orElse(Lists.newArrayList())
                .stream().map(ApmSpaceAppCustomViewRolePo::getRoleBid).collect(Collectors.toList());
    }
}




