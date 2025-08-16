package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewPo;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @description
 * @createDate 2023-09-20 16:15:29
 */
public interface ApmSpaceAppCustomViewService extends IService<ApmSpaceAppCustomViewPo> {
    /**
     * 方法描述
     *
     * @param bid bid
     * @return 返回值
     */
    Boolean removeByBid(String bid);

    /**
     * 方法描述
     *
     * @param viewModel viewModel
     * @return 返回值
     */
    List<ApmSpaceAppCustomViewPo> listByCondition(ApmSpaceAppCustomViewPo viewModel);

    /**
     * 方法描述
     *
     * @param bid        bid
     * @param enableFlag enableFlag
     * @return 返回值
     */
    Boolean changeEnableFlag(String bid, Integer enableFlag);

    /**
     * 方法描述
     *
     * @param appBidMap appBidMap
     * @return 返回值
     */
    Boolean copy(Map<String, String> appBidMap);

    /**
     * 方法描述
     *
     * @param apmSpaceAppCustomViewPo apmSpaceAppCustomViewPo
     * @return 返回值
     */
    Boolean updateByBid(ApmSpaceAppCustomViewPo apmSpaceAppCustomViewPo);

    /**
     * 方法描述
     *
     * @param customViewBid customViewBid
     * @return 返回值
     */
    ApmSpaceAppCustomViewPo getByBid(String customViewBid);
}