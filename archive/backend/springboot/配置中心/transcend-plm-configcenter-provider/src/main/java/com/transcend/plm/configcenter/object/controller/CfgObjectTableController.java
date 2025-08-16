package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTableAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectTableDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对象表控制层
 * 只能调用AppService，不能调用实体或者仓储
 *
 * @author jie.luo1
 * @version: 1.0
 * @date 2023/01/29 18:15
 */
@Slf4j
@Api(value = "CfgObject Controller", tags = "对象表管理-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/object-table")
public class CfgObjectTableController {

    @Resource
    private CfgObjectTableDomainService cfgObjectTableDomainService;
    @Resource
    private ICfgObjectAppService objectModelAppService;

    @ApiOperation(value = "发布BaseModelCode")
    @PostMapping("/publishByBaseModelCode/{baseModelCode}")
    public Boolean publishByBaseModelCode(@PathVariable String baseModelCode,
                                      @RequestBody List<CfgObjectAttributeVo> attributes) {

        // 保存表属性
        return Boolean.TRUE;
    }

    @ApiOperation(value = "查询对象树以及锁信息")
    @GetMapping("treeAndLockInfoAndPublish")
    public List<CfgObjectTreeVo> treeAndLockInfo() {
        return objectModelAppService.treeAndLockInfo();
    }




    @ApiOperation(value = "发布")
    @PostMapping("/publish/{modelCode}")
    @CacheEvict(value = CacheNameConstant.OBJECT_TABLE, allEntries = true)
    public Boolean publishByBaseModel(@PathVariable String modelCode,
                                      @RequestBody List<String> attrBids) {
        return cfgObjectTableDomainService.publishByModelCode(modelCode, attrBids);
    }

}
