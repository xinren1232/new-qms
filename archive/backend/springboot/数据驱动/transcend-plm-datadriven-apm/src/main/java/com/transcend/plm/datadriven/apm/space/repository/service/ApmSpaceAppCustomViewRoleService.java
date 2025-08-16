package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewRolePo;

import java.util.List;
import java.util.Set;

/**
 * @author jie.luo1
 * @description
 * @createDate 2023-09-20 16:15:29
 */
public interface ApmSpaceAppCustomViewRoleService extends IService<ApmSpaceAppCustomViewRolePo> {


    /**
     * 方法描述
     *
     * @param bid bid
     * @return 返回值
     */
    List<String> listRoleBidByCustomViewBid(String bid);

    /**
     * 方法描述
     *
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @param roleBids    roleBids
     * @return 返回值
     */
    boolean batchAdd(String spaceAppBid, String bid, List<String> roleBids);

    /**
     * 方法描述
     *
     * @param bid            bid
     * @param deleteRoleBids deleteRoleBids
     * @return 返回值
     */
    boolean remove(String bid, List<String> deleteRoleBids);

    /**
     * 方法描述
     *
     * @param roleBids roleBids
     * @return 返回值
     */
    List<String> listCustomViewBidByRoleBids(List<String> roleBids);
}