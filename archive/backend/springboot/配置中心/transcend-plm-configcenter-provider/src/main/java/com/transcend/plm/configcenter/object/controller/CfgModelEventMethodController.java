package com.transcend.plm.configcenter.object.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.application.service.ICfgModelEventMethodDomainService;
import com.transcend.plm.configcenter.api.model.object.dto.CfgModelEventMethodDto;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * ModelEventMethodController
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:33
 */
@RestController
@Api(value = "CfgObjectEventMethod Controller", tags = "对象事件方法绑定-控制器")
@RequestMapping(value ="/manager/cfg/object/eventMethod")
public class CfgModelEventMethodController {

    @Resource
    private ICfgModelEventMethodDomainService modelEventMethodService;

    /**
     * 保存或更新
     * @param cfgModelEventMethodDto
     * @return
     */
    @ApiOperation("新增或修改方法绑定")
    @PostMapping("/saveOrUpdate")
    public CfgModelEventMethodVo saveOrUpdate(@RequestBody CfgModelEventMethodDto cfgModelEventMethodDto) {
        return modelEventMethodService.saveOrUpdate(cfgModelEventMethodDto);
    }

    /**
     * 查看详情
     * @param bid
     * @return
     */
    @GetMapping("/get/{bid}")
    @ApiOperation("查看方法绑定详情")
    public CfgModelEventMethodVo getByBid(@PathVariable("bid") String bid) {
        return modelEventMethodService.getByBid(bid);
    }

    @ApiOperation("方法绑定分页查询")
    @PostMapping("/page")
    public PagedResult<CfgModelEventMethodVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgModelEventMethodQo> pageQo) {
        return modelEventMethodService.page(pageQo);
    }

    @ApiOperation("方法绑定批量新增")
    @PostMapping("/bulkAdd")
    public List<CfgModelEventMethodVo> bulkAdd(@RequestBody List<CfgModelEventMethodDto> dtos) {
        return modelEventMethodService.bulkAdd(dtos);
    }

    @PostMapping("/logicalDelete/{bid}")
    @ApiOperation("方法绑定删除")
    public Boolean logicalDeleteByBid(@PathVariable("bid") String bid) {
        return modelEventMethodService.logicalDeleteByBid(bid);
    }

    @PostMapping("/logicalBulkDelete")
    @ApiOperation("方法绑定批量删除")
    public Boolean logicalBulkDelete(@RequestBody List<String> bids) {
        return modelEventMethodService.logicalBulkDelete(bids);
    }

}
