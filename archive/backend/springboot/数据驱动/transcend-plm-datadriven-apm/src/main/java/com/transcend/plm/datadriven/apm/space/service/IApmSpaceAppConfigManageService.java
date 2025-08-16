package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppTabDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppTab;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface IApmSpaceAppConfigManageService {

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
     * listViewModel
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<AppViewModelDto>}
     */
    List<AppViewModelDto> listViewModelByPermission(String spaceAppBid);

    /**
     * baseViewGet
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link CfgViewVo}
     */
    CfgViewVo baseViewGet(String spaceAppBid);

    /**
     * baseViewSaveOrUpdate
     *
     * @param spaceAppBid spaceAppBid
     * @param content     content
     * @return {@link CfgViewVo}
     */
    CfgViewVo baseViewSaveOrUpdate(String spaceAppBid, Map<String, Object> content);

    /**
     * baseViewListAttributes
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<CfgObjectAttributeVo>}
     */
    List<CfgObjectAttributeVo> baseViewListAttributes(String spaceAppBid);

    /**
     * baseViewListTargetRelation
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<CfgObjectRelationVo>}
     */
    List<CfgObjectRelationVo> baseViewListTargetRelation(String spaceAppBid);

    /**
     * viewModelChangeEnableFlag
     *
     * @param spaceAppBid   spaceAppBid
     * @param viewModelCode viewModelCode
     * @param enableFlag    enableFlag
     * @return {@link Boolean}
     */
    Boolean viewModelChangeEnableFlag(String spaceAppBid, String viewModelCode, Integer enableFlag);

    /**
     * relationListTab
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<CfgObjectRelationVo>}
     */
    List<CfgObjectRelationVo> relationListTab(String spaceAppBid);

    /**
     * getViewModelDetail
     *
     * @param spaceAppBid   spaceAppBid
     * @param viewModelCode viewModelCode
     * @return {@link AppViewModelDto}
     */
    AppViewModelDto getViewModelDetail(String spaceAppBid, String viewModelCode);

    /**
     * getLifeCycleState
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link ApmLifeCycleStateVO}
     */
    ApmLifeCycleStateVO getLifeCycleState(String spaceAppBid);

    /**
     * 获取视图的元数据列表
     *
     * @param spaceAppBids 空间应用bid
     * @return 状态列表
     */
    List<ApmStateVO> getMultiLifeCycleState(List<String> spaceAppBids);

    /**
     * getLifeCycleStateByModelCode
     *
     * @param spaceBid  spaceBid
     * @param modelCode modelCode
     * @return {@link ApmLifeCycleStateVO}
     */
    ApmLifeCycleStateVO getLifeCycleStateByModelCode(String spaceBid, String modelCode);

    /**
     * listNextStates
     *
     * @param apmStateQo apmStateQo
     * @return {@link List<ApmStateVO>}
     */
    List<ApmStateVO> listNextStates(ApmStateQo apmStateQo);

    /**
     * copyViews
     *
     * @param viewBidMap viewBidMap
     * @param type       type
     * @return {@link boolean}
     */
    boolean copyViews(Map<String, String> viewBidMap, String type);

    /**
     * viewModelUpdatePartialContent
     *
     * @param spaceAppBid   spaceAppBid
     * @param viewModelCode viewModelCode
     * @param configContent configContent
     * @return {@link Boolean}
     */
    Boolean viewModelUpdatePartialContent(String spaceAppBid, String viewModelCode, Map<String, Object> configContent);

    /**
     * viewModelUpdate
     *
     * @param spaceAppBid     spaceAppBid
     * @param viewModelCode   viewModelCode
     * @param appViewModelDto appViewModelDto
     * @return {@link Boolean}
     */
    Boolean viewModelUpdate(String spaceAppBid, String viewModelCode, AppViewModelDto appViewModelDto);

    /**
     * flowNodeViewGet
     *
     * @param spaceAppBid spaceAppBid
     * @param action      action
     * @param nodeBid     nodeBid
     * @return {@link CfgViewVo}
     */
    CfgViewVo flowNodeViewGet(String spaceAppBid, String action, String nodeBid);

    /**
     * getAllFlowView
     *
     * @param spaceAppBid spaceAppBid
     * @param flowBid     flowBid
     * @param version     version
     * @return {@link List<Object>}
     */
    List<Object> getAllFlowView(String spaceAppBid, String flowBid, String version);

    /**
     * flowNodeViewSaveOrUpdate
     *
     * @param spaceAppBid spaceAppBid
     * @param nodeBid     nodeBid
     * @param action      action
     * @param content     content
     * @return {@link CfgViewVo}
     */
    CfgViewVo flowNodeViewSaveOrUpdate(String spaceAppBid, String nodeBid, String action, Map<String, Object> content);

    /**
     * saveApmSpaceAppTab
     *
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param apmSpaceAppTabDto apmSpaceAppTabDto
     * @return {@link boolean}
     */
    boolean saveApmSpaceAppTab(String spaceBid, String spaceAppBid, ApmSpaceAppTabDto apmSpaceAppTabDto);

    /**
     * relationAppListTab
     *
     * @param apmSpaceTabQo apmSpaceTabQo
     * @return {@link List<ApmObjectRelationAppVo>}
     */
    List<ApmObjectRelationAppVo> relationAppListTab(ApmSpaceTabQo apmSpaceTabQo);

    /**
     * getAppModelCodes
     *
     * @param spaceAppBid       spaceAppBid
     * @param targetSpaceAppBid targetSpaceAppBid
     * @return {@link List<String>}
     */
    List<String> getAppModelCodes(String spaceAppBid, String targetSpaceAppBid);

    /**
     * baseViewSaveOrUpdate
     *
     * @param spaceAppBid spaceAppBid
     * @param type        type
     * @param content     content
     * @return {@link CfgViewVo}
     */
    CfgViewVo baseViewSaveOrUpdate(String spaceAppBid, String type, Map<String, Object> content);

    /**
     * 查询视图
     *
     * @param spaceAppBid spaceAppBid
     * @param viewScope   viewScope
     * @param viewType    viewType
     * @return {@link CfgViewVo}
     */

    CfgViewVo baseViewGet(String spaceAppBid, String viewScope, String viewType);

    /**
     * baseViewGet
     *
     * @param spaceAppBid spaceAppBid
     * @param type        type
     * @return {@link CfgViewVo}
     */
    CfgViewVo baseViewGet(String spaceAppBid, String type);

    /**
     * listCrossRelationByModelCode
     *
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @param sourceModelCode sourceModelCode
     * @return {@link List<ApmCrossRelationVO>}
     */
    List<ApmCrossRelationVO> listCrossRelationByModelCode(String spaceBid, String spaceAppBid, String sourceModelCode);

    /**
     * downloadImportTemplate
     *
     * @param spaceAppBid spaceAppBid
     * @param type        type
     * @param response    response
     */
    void downloadImportTemplate(String spaceAppBid, String type, HttpServletResponse response);

    /**
     * getAttributeConfigByCode
     *
     * @param code code
     * @return {@link CfgAttributeVo}
     */
    CfgAttributeVo getAttributeConfigByCode(String code);

    /**
     * listRelationByModelCode
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @return {@link List<CfgObjectRelationVo>}
     */
    List<CfgObjectRelationVo> listRelationByModelCode(String spaceBid, String spaceAppBid);

    /**
     * listPhaseLifeCycleState
     *
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link List<CfgLifeCycleTemplateKeyPathNodeVo>}
     */
    List<CfgLifeCycleTemplateKeyPathNodeVo> listPhaseLifeCycleState(String spaceAppBid, String instanceBid);

    /**
     * 根据参数查询视图
     *
     * @param param 查询参数
     * @return 视图
     */
    CfgViewVo getView(ViewQueryParam param);

    /**
     * 保存或更新视图
     *
     * @param cfgViewDto 视图
     * @return 视图
     */
    CfgViewVo saveOrUpdateView(CfgViewDto cfgViewDto);

    /**
     * 通过belongBid查询视图列表
     *
     * @param belongBid 视图所属bid
     * @return 视图列表
     */
    List<CfgViewVo> listView(String belongBid);

    /**
     * 获取视图的元数据列表
     *
     * @param spaceBid        空间Bid
     * @param spaceAppBid     空间应用bid（即view的belongBid）
     * @param currentSpaceBid 当前空间Bid（应对夸空间问题）
     * @param viewModelType   视图模型类型（如：multiTreeView表示多维表格）
     * @return 元数据
     */
    MultiViewMetaListVo getViewMetaList(String spaceBid, String spaceAppBid, String currentSpaceBid, String viewModelType);

    /**
     * 获取视图元数据列表
     *
     * @param spaceBid        空间Bid
     * @param spaceAppBid     空间应用bid（即view的belongBid）
     * @param currentSpaceBid 当前空间Bid（应对夸空间问题）
     * @param viewModelType   视图模型类型（如：multiTreeView表示多维表格）
     * @return 元数据
     */
    List<ViewMetaListVo> listViewMetaList(String spaceBid, String spaceAppBid, String currentSpaceBid, String viewModelType);


    Boolean checkTabPermission(ApmSpaceAppTab apmSpaceAppTab, List<String> userRoleBids, MObject instanceData);

}
