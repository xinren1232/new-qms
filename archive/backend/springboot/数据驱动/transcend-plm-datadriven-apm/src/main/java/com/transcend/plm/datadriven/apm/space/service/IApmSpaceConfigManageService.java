package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceObjectVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceViewTreeVo;

import java.util.List;

/**
 * @author unknown
 */
public interface IApmSpaceConfigManageService {

    /**
     * batchSaveViewModel
     *
     * @param spaceAppBid      spaceAppBid
     * @param appViewModelDtos appViewModelDtos
     * @return {@link Boolean}
     */
    Boolean batchSaveViewModel(String spaceAppBid, List<AppViewModelDto> appViewModelDtos);

    /**
     * listViewModel
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<AppViewModelDto>}
     */
    List<AppViewModelDto> listViewModel(String spaceAppBid);

    /**
     * addApp
     *
     * @param spaceBid       spaceBid
     * @param apmSpaceAppDto apmSpaceAppDto
     * @return {@link List<AppViewModelDto>}
     */
    List<AppViewModelDto> addApp(String spaceBid, ApmSpaceAppDto apmSpaceAppDto);

    /**
     * appList
     *
     * @param spaceBid spaceBid
     * @return {@link List<AppViewModelDto>}
     */
    List<AppViewModelDto> appList(String spaceBid);

    /**
     * appChangeEnableFlag
     *
     * @param bid        bid
     * @param enableFlag enableFlag
     * @return {@link Boolean}
     */
    Boolean appChangeEnableFlag(String bid, Integer enableFlag);

    /**
     * listChildrenByModelCode
     *
     * @param spaceBid  spaceBid
     * @param modelCode modelCode
     * @return {@link List<ApmSpaceObjectVo>}
     */
    List<ApmSpaceObjectVo> listChildrenByModelCode(String spaceBid, String modelCode);

    /**
     * 同模型视图树
     *
     * @param viewBid 视图bid
     * @return {@link List< ApmSpaceViewTreeVo >}
     */
    List<ApmSpaceViewTreeVo> sameModelViewTree(String viewBid);
}
