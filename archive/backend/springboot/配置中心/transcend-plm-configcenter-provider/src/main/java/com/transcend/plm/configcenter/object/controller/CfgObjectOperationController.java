package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.object.domain.service.CfgObjectOperationDomainService;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectPermissionDomainService;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectOperationVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * CfgObjectOperationController
 *
 * @author jie.Luo1
 * @version: 1.0
 * @date 2022/12/12 10:26
 */
@Slf4j
@Api(value = "CfgObjectOperation Controller", tags = "对象管理-操作-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/object/operation")
public class CfgObjectOperationController {

    @Resource
    private CfgObjectOperationDomainService cfgObjectOperationDomainService;


    @ApiOperation("根据modelCode查询权限按钮列表")
    @GetMapping("/listByBaseModel/{baseModel}")
    public List<CfgObjectOperationVo> listByBaseModel(@PathVariable String baseModel) {
        return cfgObjectOperationDomainService.listByBaseModel(baseModel);
    }

}
