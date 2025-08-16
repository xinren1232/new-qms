package com.transcend.plm.configcenter.filemanagement.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.*;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileCopyRule;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibrary;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileType;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileViewer;
import com.transcend.plm.configcenter.filemanagement.pojo.dto.*;
import com.transcend.plm.configcenter.filemanagement.service.ICfgFileConfigAppService;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Api(value = "CfgFileConfigAppController", tags = "文件规则配置-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/filemanagement/")
public class CfgFileConfigAppController {
    @Resource
    private ICfgFileConfigAppService cfgFileConfigAppService;

    @ApiOperation("文件库新增或者编辑")
    @PostMapping("saveOrUpdateCfgFileLibrary")
    public CfgFileLibrary saveOrUpdateCfgFileLibrary(@RequestBody CfgFileLibraryDto cfgFileLibraryDto){
        return cfgFileConfigAppService.saveCfgFileLibrary(cfgFileLibraryDto);
    }

    @ApiOperation("复制规则新增或修改")
    @PostMapping("cfgFileCopyRule/saveOrUpdate")
    public CfgFileCopyRule saveCfgFileCopyRule(@RequestBody CfgFileCopyRuleDto dto){
        return cfgFileConfigAppService.saveCfgFileCopyRule(dto);
    }

    @ApiOperation("复制规则新增或修改")
    @PostMapping("cfgFileCopyRule/updateEnable")
    public Boolean updateCfgFileCopyRuleEnable(@RequestBody CfgFileCopyRuleDto dto){
        return cfgFileConfigAppService.updateCfgFileCopyRuleEnable(dto);
    }

    @ApiOperation("复制规则分页查询")
    @PostMapping("cfgFileCopyRule/page")
    public PagedResult<CfgFileCopyRuleVo> listCfgFileCopyRule(@RequestBody BaseRequest<CfgFileCopyRuleDto> pageQo){
        return cfgFileConfigAppService.listCfgFileCopyRule(pageQo);
    }

    @ApiOperation("文件库分页查询")
    @PostMapping("cfgFileLibrary/page")
    public PagedResult<CfgFileLibraryVo> page(@RequestBody BaseRequest<CfgFileLibraryDto> pageQo){
        return cfgFileConfigAppService.page(pageQo);
    }

    @ApiOperation("文件库编辑状态")
    @PostMapping("cfgFileLibrary/updateEnable")
    public Boolean updateCfgLibraryEnableFlag(@RequestBody CfgFileLibraryDto cfgFileLibraryDto){
        return cfgFileConfigAppService.updateCfgLibraryEnableFlag(cfgFileLibraryDto);
    }
    @ApiOperation("文件类型新增或者编辑")
    @PostMapping("fileType/updateEnable")
    public Boolean updateCfgFileTypeEnableFlag(@RequestBody CfgFileTypeDto dto){
        return cfgFileConfigAppService.updateCfgFileTypeEnableFlag(dto);
    }

    @ApiOperation("文件库明细查询")
    @GetMapping("cfgFileLibrary/get/{bid}")
    public CfgFileLibraryVo getCfgFileLibrary(@PathVariable String bid){
      return cfgFileConfigAppService.getCfgFileLibrary(bid);
    }

    @ApiOperation("文件类型删除")
    @PostMapping("cfgFileLibrary/delete/{bid}")
    public Boolean deleteFileLibrary(@PathVariable String bid){
        return cfgFileConfigAppService.deleteCfgLibrary(bid);
    }

    @ApiOperation("复制规则新增或修改")
    @PostMapping("cfgFileCopyRule/delete/{bid}")
    public Boolean deleteCfgFileCopyRuleEnable(@PathVariable String bid){
        return cfgFileConfigAppService.deleteCfgFileCopyRule(bid);
    }

    @ApiOperation("查看器新增或者编辑")
    @PostMapping("fileViewer/saveOrUpdate")
    public CfgFileViewer saveOrUpdateCfgFileViewer(@RequestBody CfgFileViewerDto dto){
        return cfgFileConfigAppService.saveCfgFileViewer(dto);
    }

    @ApiOperation("查看器新增或者编辑")
    @PostMapping("fileViewer/updateEnable")
    public Boolean updateFileViewerEnable(@RequestBody CfgFileViewerDto dto){
        return cfgFileConfigAppService.updateFileViewerEnable(dto);
    }

    @ApiOperation("查看器分页查询")
    @PostMapping("fileViewer/list")
    public PagedResult<CfgFileViewerVo> listCfgFileViewer(@RequestBody BaseRequest<CfgFileViewerDto> dto){
        return cfgFileConfigAppService.listCfgFileViewer(dto);
    }

    @ApiOperation("查看器明细查询")
    @GetMapping("fileViewer/get/{bid}")
    public CfgFileViewerVo getCfgFileViewerByBid(@PathVariable String bid){
        return cfgFileConfigAppService.getCfgFileViewerByBid(bid);
    }

    @ApiOperation("查看器删除")
    @PostMapping("fileViewer/delete/{bid}")
    public Boolean deleteCfgFileViewer(@PathVariable String bid){
        return cfgFileConfigAppService.deleteCfgFileViewer(bid);
    }


    @ApiOperation("文件类型新增或者编辑")
    @PostMapping("fileType/saveOrUpdate")
    public CfgFileType saveCfgFileType(@RequestBody CfgFileTypeDto dto){
        return cfgFileConfigAppService.saveCfgFileType(dto);
    }
    @ApiOperation("文件类型分页列表")
    @PostMapping("fileType/list")
    public PagedResult<CfgFileTypeVo> listCfgFileType(@RequestBody BaseRequest<CfgFileTypeDto> dto){
        return cfgFileConfigAppService.listCfgFileType(dto);
    }

    @ApiOperation("文件类型所有启用列表")
    @GetMapping("fileType/queryAll")
    public List<CfgFileTypeVo> queryCfgFileTypeAll() {
        return cfgFileConfigAppService.queryCfgFileTypeAll();
    }

    @ApiOperation("文件类型删除")
    @PostMapping("fileType/delete/{bid}")
    public Boolean deleteFileType(@PathVariable String bid){
        return cfgFileConfigAppService.deleteFileType(bid);
    }

    @ApiOperation("复制规则和文件类型关系操作")
    @PostMapping("cfgFileTypeRuleRel/operate")
    public boolean saveOrDeleteFileTypeRuleRel(@RequestBody CfgFileTypeRuleRelDto dto){
        return cfgFileConfigAppService.saveOrDeleteFileTypeRuleRel(dto);
    }

    @ApiOperation("复制规则和文件库配置")
    @PostMapping("cfgFileLibraryRuleRel/addOrUpdate")
    public CfgFileLibraryRuleRelVo addCfgFileLibraryRuleRel(@RequestBody CfgFileLibraryRuleRelDto cfgFileLibraryRuleRelDto){
       return cfgFileConfigAppService.addCfgFileLibraryRuleRel(cfgFileLibraryRuleRelDto);
    }
    @ApiOperation("复制规则明细查询")
    @GetMapping("cfgFileCopyRule/get/{bid}")
    public CfgFileCopyRuleVo getCfgFileCopyRule(@PathVariable String bid){
        return cfgFileConfigAppService.getCfgFileCopyRule(bid);
    }
    @ApiOperation("文件类型明细查询")
    @GetMapping("fileType/get/{bid}")
    public CfgFileTypeVo getCfgFileTypeByBid(@PathVariable String bid){
        return cfgFileConfigAppService.getCfgFileTypeByBid(bid);
    }

    @ApiOperation("文件类型下的编辑器新增或者删除")
    @PostMapping("fileType/addOrDeleteFileTypeViewer")
    public boolean addOrDeleteFileTypeViewer(@RequestBody CfgFileTypeViewerRelDto cfgFileTypeViewerRelDto){
       return cfgFileConfigAppService.addOrDeleteFileTypeViewer(cfgFileTypeViewerRelDto);
    }

    @ApiOperation("文件库复制规则下面的目标文件删除")
    @PostMapping("cfgFileLibraryRuleRel/delete/{bid}")
    public boolean deleteCfgFileLibraryRuleRel(@PathVariable String bid){
        return cfgFileConfigAppService.deleteCfgFileLibraryRuleRel(bid);
    }

}
