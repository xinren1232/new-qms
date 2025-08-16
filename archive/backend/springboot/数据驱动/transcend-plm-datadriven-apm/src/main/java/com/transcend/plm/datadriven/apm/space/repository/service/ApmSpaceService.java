package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmSpaceService extends IService<ApmSpace> {
    /**
     * getSpaceBids
     *
     * @return List<String>
     */
    List<String> getSpaceBids();

    /**
     * physicsRemoveByBid
     *
     * @param bid bid
     * @return boolean
     */
    boolean physicsRemoveByBid(String bid);

    /**
     * updateSphereBid
     * @param bid bid
     * @param sphereBid sphereBid
     */
    Boolean updateSphereBid(String bid, String sphereBid);
}
