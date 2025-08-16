package com.transcend.plm.datadriven.infrastructure.draft.repository.mapper;

import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface DraftMapper {
    /**
     * saveDraftData
     *
     * @param tenantCode  tenantCode
     * @param draftDataPO draftDataPO
     * @return {@link Integer}
     */
    Integer saveDraftData(@ApiParam("tenantCode") @Param("tenantCode") String tenantCode,
                          @Param("data") DraftPO draftDataPO);

    /**
     * deleteDraftByDataBid
     *
     * @param tenantCode tenantCode
     * @param dataBid    dataBid
     * @return {@link Integer}
     */
    Integer deleteDraftByDataBid(@ApiParam("tenantCode") String tenantCode,
                                 @ApiParam("dataBid") String dataBid);

    /**
     * deleteDraftByBid
     *
     * @param tenantCode tenantCode
     * @param bid        bid
     * @return {@link Integer}
     */
    Integer deleteDraftByBid(@ApiParam("tenantCode") String tenantCode,
                             @ApiParam("bid") String bid);

    /**
     * listDraftData
     *
     * @param tenantCode   tenantCode
     * @param draftDataDTO draftDataDTO
     * @return {@link List<DraftPO>}
     */
    List<DraftPO> listDraftData(@ApiParam("tenantCode") String tenantCode,
                                DraftPO draftDataDTO);

    /**
     * getByDataBid
     *
     * @param tenantCode tenantCode
     * @param dataBid    dataBid
     * @return {@link DraftPO}
     */
    DraftPO getByDataBid(@ApiParam("tenantCode") String tenantCode,
                         @ApiParam("dataBid") String dataBid);

    /**
     * getByBid
     *
     * @param tenantCode tenantCode
     * @param bid        bid
     * @return {@link DraftPO}
     */
    DraftPO getByBid(@ApiParam("tenantCode") String tenantCode, @ApiParam("bid") String bid);

    /**
     * getByDataBids
     *
     * @param tenantCode tenantCode
     * @param dataBids   dataBids
     * @return {@link List<DraftPO>}
     */
    List<DraftPO> getByDataBids(@Param("tenantCode") String tenantCode, @Param("dataBids") List<String> dataBids);

    /**
     * getByBids
     *
     * @param tenantCode tenantCode
     * @param bids       bids
     * @return {@link List<DraftPO>}
     */
    List<DraftPO> getByBids(String tenantCode, List<String> bids);
}
