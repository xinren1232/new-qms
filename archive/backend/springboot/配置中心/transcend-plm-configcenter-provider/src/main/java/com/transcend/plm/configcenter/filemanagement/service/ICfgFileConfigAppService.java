package com.transcend.plm.configcenter.filemanagement.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.*;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileCopyRule;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibrary;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileType;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileViewer;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.*;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

public interface ICfgFileConfigAppService {

    CfgFileLibrary saveCfgFileLibrary(CfgFileLibraryDto cfgFileLibraryDto);

    boolean deleteCfgLibrary(String bid);

    boolean deleteCfgFileViewer(String bid);

    boolean deleteCfgFileCopyRule(String bid);

    boolean updateCfgLibraryEnableFlag(CfgFileLibraryDto cfgFileLibraryDto);

    boolean updateCfgFileTypeEnableFlag(CfgFileTypeDto dto);

    CfgFileType saveCfgFileType(CfgFileTypeDto dto);

    PagedResult<CfgFileCopyRuleVo> listCfgFileCopyRule(BaseRequest<CfgFileCopyRuleDto> pageQo);

    PagedResult<CfgFileTypeVo> listCfgFileType(BaseRequest<CfgFileTypeDto> dto);

    CfgFileCopyRule saveCfgFileCopyRule(CfgFileCopyRuleDto dto);

    boolean updateCfgFileCopyRuleEnable(CfgFileCopyRuleDto dto);

    CfgFileViewer saveCfgFileViewer(CfgFileViewerDto dto);

    boolean updateFileViewerEnable(CfgFileViewerDto dto);

    PagedResult<CfgFileViewerVo> listCfgFileViewer(BaseRequest<CfgFileViewerDto> dto);

    CfgFileViewerVo getCfgFileViewerByBid(String bid);

    boolean saveOrDeleteFileTypeRuleRel(CfgFileTypeRuleRelDto dto);

    List<CfgFileTypeVo> listCfgFileTypeByRuleBid(String ruleBid);

    List<CfgFileCopyRuleVo> listCfgFileCopyRuleByFileTypeBid(String fileTypeBid);

    CfgFileLibraryVo getDefaultCfgFileLibrary();

    CfgFileLibraryVo getCfgFileLibraryByAddress(String address);

    List<CfgFileLibraryVo> getCfgFileLibraryByCodes(List<String> codes);

    boolean deleteFileType(String fileTypeBid);

    PagedResult<CfgFileLibraryVo> page(BaseRequest<CfgFileLibraryDto> pageQo);

    CfgFileLibraryVo getCfgFileLibrary(String bid);

    CfgFileCopyRuleVo getCfgFileCopyRule(String bid);

    List<CfgFileCopyRuleVo> getCfgFileCopyRuleByFileTypeCode(String code);

    CfgFileLibraryRuleRelVo addCfgFileLibraryRuleRel(CfgFileLibraryRuleRelDto cfgFileLibraryRuleRelDto);

    CfgFileTypeVo getCfgFileTypeByCode(String code);

    CfgFileTypeVo getCfgFileTypeByBid(String bid);

    boolean addOrDeleteFileTypeViewer(CfgFileTypeViewerRelDto cfgFileTypeViewerRelDto);

    boolean deleteCfgFileLibraryRuleRel(String bid);

    List<CfgFileTypeRelRuleNameVo> getCfgRuleListByName(String name);

    List<CfgFileTypeVo> queryCfgFileTypeAll();

    List<CfgFileLibraryVo> listCfgLiabrary();
}
