package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author unknown
 */
public interface ApmSpaceAppService extends IService<ApmSpaceApp> {

    /**
     * getByBid
     *
     * @param spaceAppBid spaceAppBid
     * @return ApmSpaceApp
     */
    ApmSpaceApp getByBid(String spaceAppBid);

    /**
     * listSpaceInfo
     *
     * @param spaceAppBids spaceAppBids
     * @return List<ApmSpaceAppVo>
     */
    List<ApmSpaceAppVo> listSpaceInfo(List<String> spaceAppBids);

    /**
     * listSpaceInfoBySpaceBidAndModelCode
     *
     * @param spaceBid  spaceBid
     * @param modelCode modelCode
     * @return List<ApmSpaceApp>
     */
    List<ApmSpaceApp> listSpaceAppBySpaceBidAndModelCode(String spaceBid, String modelCode);

    /**
     * getApmSpaceAppBySpaceBidAndModelCode
     *
     * @param spaceBid  spaceBid
     * @param modelCode modelCode
     * @return ApmSpaceApp
     */
    ApmSpaceApp getApmSpaceAppBySpaceBidAndModelCode(String spaceBid, String modelCode);

    /**
     * listBySpaceBidsAndModelCode
     *
     * @param spaceBids spaceBids
     * @param modelCode modelCode
     * @return List<ApmSpaceApp>
     */
    List<ApmSpaceApp> listBySpaceBidsAndModelCode(List<String> spaceBids, String modelCode);

    /**
     * listSpaceAppWithOrder
     *
     * @param spaceBid spaceBid
     * @param isAsc    isAsc
     * @return List<ApmSpaceApp>
     */
    List<ApmSpaceApp> listSpaceAppWithOrder(String spaceBid, Boolean isAsc);

    /**
     * 根据spaceBids查询spaceApp列表
     *
     * @param spaceBids 空间Bid列表
     * @return 空间Bid对应的空间App列表
     */
    Map<String, List<ApmSpaceAppVo>> listSpaceAppVoBySpaceBids(Set<String> spaceBids);

    /**
     * listSpaceAppVoByBids
     *
     * @param bids bids
     * @return List<ApmSpaceAppVo>
     */
    List<ApmSpaceAppVo> listSpaceAppVoByBids(List<String> bids);

    /**
     * 更新排序
     *
     * @param apmSpaceApps 排序列表
     */
    void updateSort(List<ApmSpaceApp> apmSpaceApps);

    /**
     * getBySpaceBidAndModelCode
     *
     * @param spaceBid  spaceBid
     * @param modelCode modelCode
     * @return ApmSpaceApp
     */
    ApmSpaceApp getBySpaceBidAndModelCode(String spaceBid, String modelCode);

    /**
     * listSpaceAppVoBySpaceBidAndModelCodes
     *
     * @param spaceBid   spaceBid
     * @param modelCodes modelCodes
     * @return List<ApmSpaceAppVo>
     */
    List<ApmSpaceAppVo> listSpaceAppVoBySpaceBidAndModelCodes(String spaceBid, List<String> modelCodes);

    /**
     * getAllSpaceAppBids
     *
     * @param modelCode modelCode
     * @return Set<String>
     */
    Set<String> getAllSpaceAppBids(String modelCode);

    /**
     * 根据spaceBid和modelCodeList获取空间应用列表
     *
     * @param spaceBid      空间Bid
     * @param modelCodeList modelCode列表
     * @return 空间应用Bid列表
     */
    List<String> getSpaceBidAndModelCodesBids(String spaceBid, List<String> modelCodeList);
}
