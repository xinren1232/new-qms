package com.transcend.plm.datadriven.apm.permission.controller;


import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmCopyRoleAndIdentityQo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleIdentityAddAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleIdentityVO;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @version 1.0.0
 * @Description 角色操作管理控制器
 * @createTime 2023-09-20 14:58:00
 */
@RestController
@RequestMapping("/apm/roleIdentity")
@Api(value = "ApmRoleIdentityController", tags = "敏捷项目管理-角色人员组织管理")
public class ApmRoleIdentityController {

    @Resource
    private IApmRoleIdentityDomainService apmRoleIdentityDomainService;

    /**
     * 修改
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("新增或者修改操作")
    public TranscendApiResponse<Boolean> saveOrUpdate(@RequestBody ApmRoleIdentityAddAO apmRoleIdentityAddAO) {
        return apmRoleIdentityDomainService.saveOrUpdate(apmRoleIdentityAddAO);
    }
    /**
     * 查询
     * @return
     */
    @GetMapping( "/list/{roleBid}/{type}")
    @ApiOperation("根据角色bid查询")
    public TranscendApiResponse<List<ApmRoleIdentityVO>> listByRoleId(@ApiParam("roleBid") @PathVariable("roleBid") String roleBid,
                                                                      @ApiParam("type") @PathVariable("type") String type,
                                                                      @ApiParam("name") @RequestParam(value = "name", required = false) String name) {
        return TranscendApiResponse.success(apmRoleIdentityDomainService.listByRoleBid(roleBid, type, name));
    }

    /**
     * 根据id删除
     * @return
     */
    @PostMapping("/delete/{id}")
    @ApiOperation("根据角色bid查询")
    public TranscendApiResponse<Boolean> listByRoleId(@ApiParam("id") @PathVariable("id") Integer id){
        return TranscendApiResponse.success(apmRoleIdentityDomainService.deleteById(id));
    }


    @PostMapping("/copyRoleAndIdentity")
    @ApiOperation("复制角色和成员")
    public TranscendApiResponse<Boolean> copyRoleAndIdentity(@RequestBody ApmCopyRoleAndIdentityQo qo) {
        apmRoleIdentityDomainService.copyRoleAndIdentity(qo);
        return TranscendApiResponse.success(true);
    }

    @GetMapping("/space/Identity/list/{spaceBid}/{roleCode}/{type}")
    @ApiOperation("查询空间某个角色的配置列表")
    public TranscendApiResponse<List<ApmRoleIdentityVO>> spaceIdentityList(
            @ApiParam("空间bid") @PathVariable("spaceBid") String spaceBid,
            @ApiParam("角色编码") @PathVariable("roleCode") String roleCode,
            @ApiParam("查询类型") @PathVariable("type") String type,
            @ApiParam("名称模糊搜索") @RequestParam(value = "name", required = false) String name) {
        return TranscendApiResponse.success(apmRoleIdentityDomainService.spaceIdentityList(spaceBid, roleCode, type, name));
    }
}
