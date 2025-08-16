package com.transcend.plm.configcenter.lifecycle.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.lifecycle.application.service.ILifeCycleApplicationService;
import com.transcend.plm.configcenter.lifecycle.domain.service.LifeCycleStateService;
import com.transcend.plm.configcenter.lifecycle.domain.service.LifeCycleTemplateService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVersionVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.LifeCycleStateExportVO;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.LifeCycleStateImportVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Author: yuanhu.huang
 * Date: 2023/2/22
 * Time: 上午11:58
 * Describe:  生命周期控制器
 */
@Slf4j
@Api(value = "LifeCycle Controller", tags = "生命周期-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/life-cycle/")
public class LifeCycleController {
    @Resource
    private ILifeCycleApplicationService lifeCycleApplicationService;
    @Resource
    private LifeCycleStateService lifeCycleStateService;
    @Resource
    private LifeCycleTemplateService lifeCycleTemplateService;

    @ApiOperation("状态分页查询")
    @PostMapping("state/page")
    public PagedResult<LifeCycleStateVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<LifeCycleStateListQo> pageQo) {
        return lifeCycleApplicationService.page(pageQo);
    }
    @ApiOperation("查询所有")
    @PostMapping("state/list")
    public List<LifeCycleStateVo> list(@RequestBody LifeCycleStateListQo lifeCycleStateListQo){
        return lifeCycleStateService.list(lifeCycleStateListQo);
    }

    @ApiOperation("查询所有")
    @PostMapping("state/allList")
    public List<LifeCycleStateVo> allList(){
        return lifeCycleStateService.allList();
    }

    @PostMapping("state/exportLifeCycleState")
    @ApiOperation(value = "导出生命周期状态", httpMethod = "POST")
    public void exportLifeCycleState(HttpServletResponse response, @RequestBody LifeCycleStateListQo lifeCycleStateListQo) throws IOException {
        String fileName = URLEncoder.encode("生命周期状态", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        setReponseHeadAsExcel(response,fileName + ExcelTypeEnum.XLSX.getValue());
        List<LifeCycleStateVo> lifeCycleStateVos = lifeCycleStateService.list(lifeCycleStateListQo);
        List<LifeCycleStateExportVO> lifeCycleStateExportVOS = BeanUtil.copy(lifeCycleStateVos,LifeCycleStateExportVO.class);
        int num = 1;
        for(LifeCycleStateExportVO lifeCycleStateExportVO:lifeCycleStateExportVOS){
            lifeCycleStateExportVO.setNum((num++)+"");
        }
        EasyExcel.write(response.getOutputStream(), LifeCycleStateExportVO.class).sheet("生命周期状态导出结果").doWrite(lifeCycleStateExportVOS);
    }

    @PostMapping("state/importData")
    @ApiOperation(value = "导入数据" ,httpMethod = "POST")
    public LifeCycleStateImportVo importData(@ApiParam(name = "文件") MultipartFile file) throws IOException {
       return lifeCycleStateService.importData(file);
    }



    @ApiOperation("生命周期状态新增或修改")
    @PostMapping("state/saveOrUpdate")
    public LifeCycleStateVo saveOrUpdate(@RequestBody LifeCycleStateDto dto) {
        if(StringUtil.isBlank(dto.getBid())){
            return lifeCycleApplicationService.add(dto);
        }else{
            return lifeCycleApplicationService.edit(dto);
        }
    }

    /*@ApiOperation("修改")
    @PostMapping("state/edit")
    public LifeCycleStateVo edit(@RequestBody LifeCycleStateDto dto) {
        return lifeCycleApplicationService.edit(dto);
    }*/

    @PostMapping("state/delete/{bid}")
    @ApiOperation(value="删除生命周期状态接口",httpMethod = "POST")
    public boolean delete(@PathVariable(name="bid") String bid) {
        return lifeCycleApplicationService.delete(bid);
    }
    @ApiOperation("保存excel导入的数据")
    @PostMapping("state/saveImport")
    public List<LifeCycleStateVo> saveImport(@RequestBody List<LifeCycleStateDto> dtos){
       return lifeCycleStateService.saveImport(dtos);
    }
    @ApiOperation("模板分页查询")
    @PostMapping("template/page")
    public PagedResult<CfgLifeCycleTemplateVo> templatePage(@RequestBody BaseRequest<CfgLifeCycleTemplateQo> pageQo) {
        return lifeCycleTemplateService.page(pageQo);
    }

    @ApiOperation("生命周期模板新增或修改")
    @PostMapping("template/saveOrUpdate")
    public CfgLifeCycleTemplateVo saveOrUpdate(@RequestBody CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        if(StringUtil.isBlank(cfgLifeCycleTemplateDto.getBid())){
            return lifeCycleTemplateService.add(cfgLifeCycleTemplateDto);
        }else{
            return lifeCycleTemplateService.edit(cfgLifeCycleTemplateDto);
        }
    }

    @ApiOperation("设置模板状态(启用未启用)")
    @PostMapping("template/changeEnableFlag")
    public boolean changeEnableFlag(@RequestBody CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
       return lifeCycleTemplateService.setEnable(cfgLifeCycleTemplateDto);
    }

    /*@ApiOperation("修改模板")
    @PostMapping("template/edit")
    public CfgLifeCycleTemplateVo editTemplate(@RequestBody CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        return lifeCycleTemplateService.edit(cfgLifeCycleTemplateDto);
    }*/
    @ApiOperation("设置模板版本")
    @PostMapping("template/changeVersion")
    public boolean changeVersion(@RequestBody CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
       return lifeCycleTemplateService.setVersion(cfgLifeCycleTemplateDto);
    }
    @ApiOperation("获取模板版本记录")
    @PostMapping("template/listVersion")
    public List<CfgLifeCycleTemplateVersionVo> listVersion(@RequestBody CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto){
        return lifeCycleTemplateService.getVersions(cfgLifeCycleTemplateDto);
    }
    @ApiOperation(value="删除生命周期模板",httpMethod = "POST")
    @PostMapping("template/logicalDelete/{bid}")
    public boolean deleteTemplate(@PathVariable(name="bid") String bid) {
        return lifeCycleTemplateService.deleteTemplate(bid);
    }
    @ApiOperation("获取单个模板明细数据")
    @PostMapping("template/getOne")
    public CfgLifeCycleTemplateVo getCfgLifeCycleTemplateVo(@RequestBody TemplateDto templateDto) {
        return lifeCycleTemplateService.getCfgLifeCycleTemplateVo(templateDto);
    }

    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("template/getTemplateNodes")
    public List<CfgLifeCycleTemplateNodePo> getTemplateNodes(@RequestBody TemplateDto templateDto){
        return lifeCycleTemplateService.getTemplateNodes(templateDto);
    }

    @ApiOperation("获取当前生命周期状态的下一个状态")
    @PostMapping("template/getNextTemplateNodes")
    @Cacheable(value = CacheNameConstant.API_LIFE_CYCLE_TEMPLATE_NODE, key = "#templateDto.templateBid+'_'+#templateDto.version+'_'+#templateDto.currentLifeCycleCode")
    public List<CfgLifeCycleTemplateNodePo> getNextTemplateNodes(@RequestBody TemplateDto templateDto){
        return lifeCycleTemplateService.getNextTemplateNodes(templateDto);
    }

    @ApiOperation("查询阶段生命周期模板所有关键路径")
    @PostMapping("template/getKeyPathNodes")
    //@Cacheable(value = CacheNameConstant.API_LIFE_CYCLE_TEMPLATE_NODE, key = "#templateDto.templateBid+'_'+#templateDto.version")
    public List<CfgLifeCycleTemplateNodePo> getKeyPathNodes(@RequestBody TemplateDto templateDto){
        return lifeCycleTemplateService.getKeyPathNodes(templateDto);
    }

    @ApiOperation("查询阶段生命周期节点所属关键路径")
    @PostMapping("template/getKeyPathNode")
    @Cacheable(value = CacheNameConstant.API_LIFE_CYCLE_TEMPLATE_NODE, key = "#templateDto.templateBid+'_'+#templateDto.version+'_'+#templateDto.currentLifeCycleCode")
    public CfgLifeCycleTemplateNodePo getKeyPathNode(@RequestBody TemplateDto templateDto){
        return lifeCycleTemplateService.getKeyPathNode(templateDto);
    }

    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("template/getTemplateNodesSorted")
    public List<CfgLifeCycleTemplateNodePo> getTemplateNodesOrderByLine(@RequestBody TemplateDto templateDto){
        return lifeCycleApplicationService.getTemplateNodesOrderByLine(templateDto);
    }

    private void setReponseHeadAsExcel(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8'zh_cn'" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }
}
