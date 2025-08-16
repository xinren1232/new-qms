package com.transcend.plm.datadriven.apm.permission.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgRoleFeignClient;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmRoleQO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色管理控制器
 * @createTime 2023-09-20 14:58:00
 */
@RestController
@RequestMapping("/apm/role")
@Api(value = "ApmRoleController", tags = "敏捷项目管理-角色管理")
public class ApmRoleController {

    @Resource
    private IApmRoleDomainService apmRoleService;


    @Resource
    private CfgRoleFeignClient cfgRoleFeignClient;

    /**
     * 新增角色
     *
     * @param apmRoleAO 角色信息
     * @return 新增后的角色信息
     */
    @PostMapping("/add")
    @ApiOperation("新增角色")
    public TranscendApiResponse<ApmRoleVO> add(@Valid @RequestBody ApmRoleAO apmRoleAO) {
        return TranscendApiResponse.success(apmRoleService.add(apmRoleAO));
    }

    /**
     * 编辑角色
     *
     * @param apmRoleAO 角色信息
     * @return 编辑后的角色信息
     */
    @PostMapping("/update")
    @ApiOperation("编辑角色")
    @CachePut(value = CacheNameConstant.ROLE, key = "#apmRoleAO.bid")
    public TranscendApiResponse<ApmRoleVO> update(@Valid @RequestBody ApmRoleAO apmRoleAO) {
        return TranscendApiResponse.success(apmRoleService.update(apmRoleAO));
    }

    /**
     * 查看角色信息
     *
     * @param bid 角色bid
     * @return 角色信息
     */
    @GetMapping("/get/{bid}")
    @ApiOperation("查看角色信息")
    @Cacheable(value = CacheNameConstant.ROLE, key = "#bid")
    public TranscendApiResponse<ApmRoleVO> get(@PathVariable String bid) {
        return TranscendApiResponse.success(apmRoleService.getByBid(bid));
    }

    /**
     * 查询角色列表
     *
     * @param apmRoleQO 查询条件
     * @return 角色列表
     */
    @PostMapping("/list")
    @ApiOperation("查询角色列表")
    public TranscendApiResponse<List<ApmRoleVO>> list(@RequestBody ApmRoleQO apmRoleQO) {
        return TranscendApiResponse.success(apmRoleService.list(apmRoleQO));
    }


    /**
     * 删除角色
     *
     * @param bid 角色bid
     * @return 是否删除成功
     */
    @PostMapping("/delete/{bid}")
    @ApiOperation("删除角色")
    @CacheEvict(value = CacheNameConstant.ROLE, key = "#bid")
    public TranscendApiResponse<Boolean> delete(@PathVariable String bid) {
        return TranscendApiResponse.success(apmRoleService.deleteByBid(bid));
    }

    /**
     * 根据角色获取人员列表
     *
     * @param bid 角色bid
     * @return 人员列表
     */
    @GetMapping("/getUsersByRole/{bid}")
    @ApiOperation("根据角色获取人员列表")
    public TranscendApiResponse<List<ApmUser>> getUsersByRole(@PathVariable String bid) {
        return TranscendApiResponse.success(apmRoleService.getIdentityByRole(bid).getApmUserList());
    }

    @PostMapping("/addSphereAndDefaultRole")
    @ApiOperation("添加域和默认角色")
    public TranscendApiResponse<Boolean> addSphereAndDefaultRole() {
        apmRoleService.addSphereAndDefaultRole();
        return TranscendApiResponse.success(true);
    }

    /**
     * 查询登录人是否为空间管理员角色
     *
     * @param spaceBid 空间bid
     * @return 是否为空间管理员角色
     */
    @GetMapping("/isSpaceAdmin/{spaceBid}")
    @ApiOperation("是否为空间管理员")
    public TranscendApiResponse<Boolean> isSpaceAdmin(@PathVariable String spaceBid) {
        return TranscendApiResponse.success(apmRoleService.isSpaceAdmin(spaceBid));
    }

    @GetMapping("/isGlobalAdmin")
    @ApiOperation("是否为超管")
    public TranscendApiResponse<Boolean> isGlobalAdmin() {
        return TranscendApiResponse.success(apmRoleService.isGlobalAdmin());
    }

    /**
     * 查系统角色树
     *
     * @return 查角色树
     */
    @GetMapping("/sys/tree")
    @ApiOperation("查系统角色树")
    public TranscendApiResponse<List<CfgRoleVo>> tree() {
        return TranscendApiResponse.success(cfgRoleFeignClient.tree().getCheckExceptionData());
    }
}
