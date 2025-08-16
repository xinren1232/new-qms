package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppViewConfigDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppViewConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewUserRecord;

import java.util.List;

/**
 * @author unknown
 */
public interface IApmAppViewConfigAppService {
    /**
     * saveAppViewConfig
     *
     * @param apmAppViewConfigDto apmAppViewConfigDto
     * @return {@link boolean}
     */
    boolean saveAppViewConfig(ApmAppViewConfigDto apmAppViewConfigDto);

    /**
     * listViewConfigs
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param tabBid  tabBid
     * @return {@link List<ApmAppViewConfigVo>}
     */
    List<ApmAppViewConfigVo> listViewConfigs(String spaceBid, String spaceAppBid, String tabBid);

    /**
     * deleteByBid
     *
     * @param bid bid
     * @return {@link boolean}
     */
    boolean deleteByBid(String bid);

    /**
     * saveApmAppViewUserRecord
     *
     * @param apmAppViewUserRecord apmAppViewUserRecord
     * @return {@link boolean}
     */
    boolean saveApmAppViewUserRecord(ApmAppViewUserRecord apmAppViewUserRecord);

    /**
     * getViewByBid
     *
     * @param bid bid
     * @return {@link ApmAppViewConfigVo}
     */
    ApmAppViewConfigVo getViewByBid(String bid);

    /**
     * shareAppView
     *
     * @param appViewConfigBid appViewConfigBid
     * @param spaceBidList     spaceBidList
     * @return {@link boolean}
     */
    boolean shareAppView(String appViewConfigBid, List<String> spaceBidList);

    /**
     * deleteApmAppViewUserRecord
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link boolean}
     */
    boolean deleteApmAppViewUserRecord(String spaceAppBid);
}
