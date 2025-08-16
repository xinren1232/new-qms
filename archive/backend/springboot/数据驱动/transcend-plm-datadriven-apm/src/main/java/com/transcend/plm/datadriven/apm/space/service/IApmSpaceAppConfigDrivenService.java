package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessOperationAo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigUserVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.*;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppExportTemplateVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLaneTabVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmObjectRelationAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppCustomViewVo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface IApmSpaceAppConfigDrivenService {

    /**
     * 复制操作
     *
     * @param spaceAppBidMap 空间应用bid映射
     * @param roleBidMap     角色bid映射
     * @param sphereBidMap   空间bid映射
     * @return boolean
     */
    boolean copyAccess(Map<String, String> spaceAppBidMap, Map<String, String> roleBidMap, Map<String, String> sphereBidMap);

    /**
     * 展示视图模式
     *
     * @param spaceAppBid 空间应用bid
     * @return java.util.List<com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto>
     */
    List<AppViewModelDto> listViewModel(String spaceAppBid);

    /**
     * 获取视图元数据
     *
     * @param spaceAppBid 空间应用bid
     * @return java.util.List<com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto>
     */
    List<CfgViewMetaDto> baseViewGetMeteModels(String spaceAppBid);

    /**
     * 获取关系视图元数据
     *
     * @param spaceAppBid              空间应用bid
     * @param ignoreRelationModelCodes 忽略的关系元数据编码
     * @return java.util.List<com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto>
     * @version: 1.0
     * @date: 2023/12/14/014
     * @author: yanbing.ao
     */
    List<CfgViewMetaDto> queryRelationMetaList(String spaceAppBid, List<String> ignoreRelationModelCodes);

    /**
     * tableView
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link CfgViewVo}
     */
    CfgViewVo tableView(String spaceAppBid);

    /**
     * getViewModelDetail
     *
     * @param spaceAppBid   spaceAppBid
     * @param viewModelCode viewModelCode
     * @return {@link AppViewModelDto}
     */
    AppViewModelDto getViewModelDetail(String spaceAppBid, String viewModelCode);

    /**
     * 复制视图模式（根据空间应用bid）
     *
     * @param oldSpaceAppBid 旧空间应用bid
     * @param newSpaceAppBid 新空间应用bid
     * @return boolean
     */
    Boolean copyViewModelBySpaceAppBid(String oldSpaceAppBid, String newSpaceAppBid);

    /**
     * 批量复制视图模式（根据空间应用bid）
     *
     * @param list 复制内容
     * @return boolean
     */
    Boolean batchCopyViewModelBySpaceAppBid(List<ApmSpaceAppViewModelCopyDto> list);

    /**
     * 复制空间应用（根据空间bid）
     *
     * @param oldSpaceBid    旧空间bid
     * @param newSpaceBid    新空间bid
     * @param newSpaceAppBid 新空间应用bid
     * @return boolean
     */
    Boolean copySpaceAppBySpaceBid(String oldSpaceBid, String newSpaceBid, String newSpaceAppBid);

    /**
     * 复制空间应用（根据空间bid）
     *
     * @param list 复制内容
     * @return boolean
     */
    Boolean batchCopySpaceAppBySpaceBid(List<ApmSpaceAppCopyDto> list);

    /**
     * relationAppListTab
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<ApmObjectRelationAppVo>}
     */
    List<ApmObjectRelationAppVo> relationAppListTab(String spaceAppBid);

    /**
     * queryAccessWithRoleBySphereBid
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<ApmActionConfigVo>}
     */
    List<ApmActionConfigVo> queryAccessWithRoleBySphereBid(String spaceAppBid);

    /**
     * addAccess
     *
     * @param apmAccessOperationAo apmAccessOperationAo
     * @param spaceAppBid          spaceAppBid
     * @return {@link Boolean}
     */
    Boolean addAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid);

    /**
     * updateAccess
     *
     * @param apmAccessOperationAo apmAccessOperationAo
     * @param spaceAppBid          spaceAppBid
     * @return {@link Boolean}
     */
    Boolean updateAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid);

    /**
     * currentUserAccess
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> currentUserAccess(String spaceAppBid);

    /**
     * deleteAccess
     *
     * @param apmAccessOperationAo apmAccessOperationAo
     * @param spaceAppBid          spaceAppBid
     * @return {@link Boolean}
     */
    Boolean deleteAccess(ApmAccessOperationAo apmAccessOperationAo, String spaceAppBid);

    /**
     * customViewPermissionList
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<ApmSpaceAppCustomViewVo>}
     */
    List<ApmSpaceAppCustomViewVo> customViewPermissionList(String spaceAppBid);

    /**
     * distributeAccessByTemplateSpace
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @return {@link Boolean}
     */
    Boolean distributeAccessByTemplateSpace(String spaceBid, String spaceAppBid);

    /**
     * customViewGet
     *
     * @param customViewBid customViewBid
     * @return {@link ApmSpaceAppCustomViewVo}
     */
    ApmSpaceAppCustomViewVo customViewGet(String customViewBid);

    /**
     * customViewSaveOrUpdate
     *
     * @param spaceAppBid              spaceAppBid
     * @param apmSpaceAppCustomViewDto apmSpaceAppCustomViewDto
     * @return {@link Boolean}
     */
    Boolean customViewSaveOrUpdate(String spaceAppBid, ApmSpaceAppCustomViewDto apmSpaceAppCustomViewDto);

    /**
     * currentUserInstanceAccess
     *
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> currentUserInstanceAccess(String spaceAppBid, String instanceBid);

    /**
     * laneTabList
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param tabBid      tabBid
     * @return {@link List<ApmLaneTabVO>}
     */
    List<ApmLaneTabVO> laneTabList(String spaceBid, String spaceAppBid, String tabBid);

    /**
     * getMultiAppConfig
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param tabBid      tabBid
     * @return {@link MultiAppConfig}
     */
    MultiAppConfig getMultiAppConfig(String spaceBid, String spaceAppBid, String tabBid);

    /**
     * objectBaseInfo
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link ObjectVo}
     */
    ObjectVo objectBaseInfo(String spaceAppBid);

    /**
     * exportTemplateSaveOrUpdate
     *
     * @param apmAppExportTemplateDto apmAppExportTemplateDto
     * @return {@link ApmAppExportTemplateVo}
     */
    ApmAppExportTemplateVo exportTemplateSaveOrUpdate(ApmAppExportTemplateDto apmAppExportTemplateDto);

    /**
     * exportTemplateDelete
     *
     * @param bid bid
     * @return {@link Boolean}
     */
    Boolean exportTemplateDelete(String bid);

    /**
     * queryExportTemplate
     *
     * @param apmAppExportTemplateDto apmAppExportTemplateDto
     * @return {@link List<ApmAppExportTemplateVo>}
     */
    List<ApmAppExportTemplateVo> queryExportTemplate(ApmAppExportTemplateDto apmAppExportTemplateDto);

    /**
     * export
     *
     * @param apmAppExportDto apmAppExportDto
     * @param response        response
     * @throws IOException IOException
     */
    void export(ApmAppExportDto apmAppExportDto, HttpServletResponse response) throws IOException;

    /**
     * 查询当前登陆人实例的操作权限
     *
     * @param spaceAppBid   spaceAppBid
     * @param instanceBid   instanceBid
     * @param apmAccessQo apmAccessQo
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> currentUserPermissionAccess(String spaceAppBid, String instanceBid, ApmAccessQo apmAccessQo);

    /**
     * 查询当前登陆人操作权限
     *
     * @param spaceAppBid   spaceAppBid
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> currentUserPermissionAccess(String spaceAppBid);

    /**
     * 根据权限查询viewModel
     *
     * @param spaceBid     spaceBid
     * @param spaceAppBid  spaceAppBid
     * @param entrance  entrance
     * @param tabBid tabBid
     * @param inverseQuery 是否翻转查询
     * @return List<AppViewModelDto>
     */
    List<AppViewModelDto> listViewModelByPermission(String spaceBid, String spaceAppBid, String entrance, String tabBid, Boolean inverseQuery);

    /**
     * 根据权限查询viewModel
     *
     * @param   apmSpaceTabQo apmSpaceTabQo
     * @return List<AppViewModelDto>
     */
    List<ApmObjectRelationAppVo> listRelationTabByPermission(ApmSpaceTabQo apmSpaceTabQo);


    /**
     * 查询当前登陆人操作权限 多树结构
     *
     * @param spaceBid
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @param query       query
     * @return List<ApmActionConfigUserVo>
     */
    List<ApmActionConfigUserVo> currentUserAccessByMultiTree(String spaceBid, String spaceAppBid, String instanceBid, ApmMultiTreeAccessQo query);

    Map<String, Object> getProperties(Map<String, Object> content, String name);
}
