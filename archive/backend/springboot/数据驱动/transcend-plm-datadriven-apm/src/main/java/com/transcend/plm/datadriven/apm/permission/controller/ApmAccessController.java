package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessAO;
import com.transcend.plm.datadriven.apm.permission.service.IApmAccessDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmAccessVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @version 1.0.0
 * @Description 操作管理控制器
 * @createTime 2023-09-20 14:58:00
 */
@RestController
@RequestMapping("/apm/access")
@Api(value = "ApmAccessController", tags = "敏捷项目管理-操作管理")
public class ApmAccessController {
    @Resource
    private IApmAccessDomainService apmAccessDomainService;

    /**
     * 新增操作
     * @param apmAccessAO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("新增操作")
    public TranscendApiResponse<ApmAccessVO> add(@RequestBody ApmAccessAO apmAccessAO) {
        return TranscendApiResponse.success(apmAccessDomainService.save(apmAccessAO));
    }

    /**
     * 修改
     * @param apmAccessAO
     * @return
     */
    @PostMapping("/update")
    @ApiOperation("修改操作")
    public TranscendApiResponse<ApmAccessVO> update(@RequestBody ApmAccessAO apmAccessAO) {
        return TranscendApiResponse.success(apmAccessDomainService.update(apmAccessAO));
    }

    /**
     * 新增角色
     * @param bid
     * @return 新增后的角色信息
     */
    @PostMapping("/logicDelete/{bid}")
    @ApiOperation("逻辑删除")
    public TranscendApiResponse<Boolean> logicDelete(@ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmAccessDomainService.logicDelete(bid));
    }

    /**
     * 查询角色列表
     * @return 新增后的角色信息
     */
    @GetMapping("/list")
    @ApiOperation("查询操作列表")
    public TranscendApiResponse<List<ApmAccessVO>> list() {
        return TranscendApiResponse.success(apmAccessDomainService.list());
    }
}
