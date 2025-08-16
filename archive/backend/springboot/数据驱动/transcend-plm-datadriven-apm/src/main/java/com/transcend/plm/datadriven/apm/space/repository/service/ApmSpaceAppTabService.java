package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface ApmSpaceAppTabService extends IService<ApmSpaceAppTab> {

    /**
     * deleteBySpaceAppBid
     *
     * @param apmSpaceAppTab apmSpaceAppTab
     * @return boolean
     */
    boolean deleteBySpaceAppBid(ApmSpaceAppTab apmSpaceAppTab);

    /**
     * enableBySpaceAppBid
     *
     * @param apmSpaceAppTab apmSpaceAppTab
     * @return boolean
     */
    boolean enableBySpaceAppBid(ApmSpaceAppTab apmSpaceAppTab);

    /**
     * listBySpaceAppBid
     *
     * @param spaceAppBid spaceAppBid
     * @return List<ApmSpaceAppTab>
     */
    List<ApmSpaceAppTab> listBySpaceAppBid(String spaceAppBid);

    /**
     * listBySpaceAppBids
     *
     * @param spaceAppBids      spaceAppBids
     * @param relationModelCode relationModelCode
     * @return List<ApmSpaceAppTab>
     */
    List<ApmSpaceAppTab> listBySpaceAppBids(List<String> spaceAppBids, String relationModelCode);

    /**
     * 根据应用bid和关系对象modelCode查询
     *
     * @param spaceAppBid       空间应用bid
     * @param relationModelCode 关系对象modelCode
     * @return ApmSpaceAppTab
     */
    ApmSpaceAppTab getBySpaceAppBidAndRelationModelCode(String spaceAppBid, String relationModelCode);

    /**
     * copyApmSpaceAbbTabs
     *
     * @param apmSpaceBidMap    apmSpaceBidMap
     * @param apmSpaceAppBidMap apmSpaceAppBidMap
     * @param sphereMap         sphereMap
     * @return boolean
     */
    boolean copyApmSpaceAbbTabs(Map<String, String> apmSpaceBidMap, Map<String, String> apmSpaceAppBidMap, Map<String, String> sphereMap);

    /**
     * getByBid
     *
     * @param tabBid tabBid
     * @return ApmSpaceAppTab
     */
    ApmSpaceAppTab getByBid(String tabBid);

    /**
     * copyApmSpaceAbbTab
     *
     * @param apmSpaceBidMap    apmSpaceBidMap
     * @param apmSpaceAppBidMap apmSpaceAppBidMap
     * @param sphereMap         sphereMap
     * @param nowSpaceBid       nowSpaceBid
     * @return boolean
     */
    boolean copyApmSpaceAbbTab(Map<String, String> apmSpaceBidMap, Map<String, String> apmSpaceAppBidMap, Map<String, String> sphereMap, String nowSpaceBid);
}
