package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;

import java.util.List;
import java.util.Set;

/**
 * @author peng.qin
 * @description 针对表【apm_domain】的数据库操作Service
 * @createDate 2023-09-20 16:15:29
 */
public interface ApmSphereService extends IService<ApmSphere> {
    /**
     * getByBizBidAndType
     *
     * @param bizBid bizBid
     * @param type   type
     * @return ApmSphere
     */
    ApmSphere getByBizBidAndType(String bizBid, String type);

    /**
     * deleteByBizBidAndType
     *
     * @param bizBid bizBid
     * @param type   type
     * @return Boolean
     */
    Boolean deleteByBizBidAndType(String bizBid, String type);

    /**
     * 通过sphereBid获取sphere
     *
     * @param bid sphereBid
     * @return ApmSphere
     */
    ApmSphere getByBid(String bid);

    /**
     * 通过下层域查询上层域 实例域查空间域，应用域查空间域
     *
     * @param sphereBids sphereBids
     * @return List<String>
     */
    List<String> querySpaceSphereBidBySphereBid(Set<String> sphereBids);

    /**
     * 通过sphereBid获取所有上层的sphereBid
     *
     * @param bid bid
     * @return List<String>
     */
    List<String> getSphereBidListByBid(String bid);

    /**
     * 通过bizBids获取sphere
     *
     * @param type    type
     * @param bizBids bizBids
     * @return List<ApmSphere>
     */
    List<ApmSphere> getByBizBids(String type, Set<String> bizBids);
}
