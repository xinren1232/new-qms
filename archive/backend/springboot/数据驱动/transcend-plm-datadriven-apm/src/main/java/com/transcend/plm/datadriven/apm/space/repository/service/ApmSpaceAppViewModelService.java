package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @description
 * @createDate 2023-09-20 16:15:29
 */
public interface ApmSpaceAppViewModelService extends IService<ApmSpaceAppViewModelPo> {
    /**
     * removeByBid
     *
     * @param bid bid
     * @return Boolean
     */
    Boolean removeByBid(String bid);

    /**
     * listByCondition
     *
     * @param viewModel viewModel
     * @return List<ApmSpaceAppViewModelPo>
     */
    List<ApmSpaceAppViewModelPo> listByCondition(ApmSpaceAppViewModelPo viewModel);

    /**
     * changeEnableFlag
     *
     * @param spaceAppBid   spaceAppBid
     * @param viewModelCode viewModelCode
     * @param enableFlag    enableFlag
     * @return Boolean
     */
    Boolean changeEnableFlag(String spaceAppBid, String viewModelCode, Integer enableFlag);

    /**
     * copyApmSpaceAppViewModel
     *
     * @param appBidMap appBidMap
     * @return Boolean
     */
    Boolean copyApmSpaceAppViewModel(Map<String, String> appBidMap);

}