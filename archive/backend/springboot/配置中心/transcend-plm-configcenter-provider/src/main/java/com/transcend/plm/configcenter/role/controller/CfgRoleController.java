package com.transcend.plm.configcenter.role.controller;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleUserVo;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.role.application.service.ICfgRoleApplicationService;
import com.transcend.plm.configcenter.role.domain.service.CfgRoleDomainService;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRoleUserPo;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
import com.transcend.plm.configcenter.role.pojo.vo.CfgRoleAndTypeVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@RestController
@Api(value = "CfgRole Controller", tags = "角色管理-控制器")
@RequestMapping(value ="/manager/cfg/role")
public class CfgRoleController {
    @Resource
    private ICfgRoleApplicationService iCfgRoleApplicationService;
    @Resource
    private CfgRoleDomainService cfgRoleDomainService;

    @ApiOperation("保存并修改基本信息")
    @PostMapping("/saveOrUpdate")
    public CfgRoleVo saveOrUpdate(@RequestBody CfgRoleDto cfgAttributeDto) {
        return iCfgRoleApplicationService.saveOrUpdate(cfgAttributeDto);
    }

    @ApiOperation("更新基本信息")
    @PostMapping("/update")
    public CfgRoleVo update(@RequestBody CfgRoleDto cfgAttributeDto) {
        return cfgRoleDomainService.update(cfgAttributeDto);
    }
    @ApiOperation("获取基本信息")
    @GetMapping("/get/{bid}")
    public CfgRoleVo getByBid(@PathVariable("bid") String bid) {
        return cfgRoleDomainService.getByBid(bid);
    }

    @ApiOperation("分页查询")
    @PostMapping("/page")
    public PagedResult<CfgRoleVo> page(@ApiParam("分页查询参数")@RequestBody BaseRequest<CfgRoleQo> pageQo) {
        return cfgRoleDomainService.page(pageQo);
    }

    @ApiOperation("查角色树")
    @GetMapping("/tree")
    public List<CfgRoleVo> tree() {
        return cfgRoleDomainService.tree();
    }


    @ApiOperation("查角色树+系统角色")
    @GetMapping("/treeAndSystem")
    public List<CfgRoleAndTypeVo> treeAndSystem() {
        return cfgRoleDomainService.treeAndSystem();
    }



    @ApiOperation("更改字典启用状态 0未启用，1启用，2禁用 bid必填，enableFlag必填")
    @PostMapping("/changeEnableFlag/{bid}/{enableFlag}")
    public Boolean changeEnableFlag(@PathVariable("bid") String bid,
                                    @PathVariable("enableFlag") Integer enableFlag) {
        return cfgRoleDomainService.changeEnableFlag(bid, enableFlag);
    }

    @ApiOperation("删除字典")
    @PostMapping("logicalDelete/{bid}")
    public Boolean logicalDelete(@PathVariable("bid") String bid) {
        return cfgRoleDomainService.logicalDelete(bid);
    }

    @ApiOperation("向角色增加用户")
    @PostMapping("/addUser")
    public Boolean addUser(@RequestBody CfgRoleUserPo cfgRoleUserPo) {
        return cfgRoleDomainService.addUser(cfgRoleUserPo);
    }

    @ApiOperation("向角色删除用户")
    @PostMapping("/removeUser/{roleBid}/{jobNumber}")
    public Boolean removeUser(@PathVariable("roleBid") String roleBid,
                              @PathVariable("jobNumber") String jobNumber) {
        return cfgRoleDomainService.removeUser(roleBid, jobNumber);
    }

    @ApiOperation("向角色批量增加用户")
    @PostMapping("/addUsers")
    public Boolean addUsers(@RequestBody List<CfgRoleUserPo> roleUserPos) {
        return cfgRoleDomainService.addUsers(roleUserPos);
    }

    @ApiOperation("根据角色编码查询所属用户")
    @GetMapping("/getUsersByRoleCode/{roleCode}")
    public List<CfgRoleUserVo> getUsersByRoleCode(@PathVariable("roleCode") String roleCode) {
        return cfgRoleDomainService.getUsersByRoleCode(roleCode);
    }

    @ApiOperation("根据用户查询所属角色")
    @GetMapping("/getRolesByUserId/{userId}")
    public List<String> getRolesByUserId(@PathVariable("userId") String jobNumber) {
        return cfgRoleDomainService.getRoleCodesByJobNumber(jobNumber);
    }

    @ApiOperation("删除角色下的所有用户然后添加")
    @PostMapping("/deleteAndAddUsers/{roleCode}")
    public Boolean deleteAndAddUsers(@RequestBody List<CfgRoleUserPo> roleUserPos,
                                     @PathVariable("roleCode") String roleCode) {
        return cfgRoleDomainService.deleteAndAddUsers(roleUserPos, roleCode);
    }
}
