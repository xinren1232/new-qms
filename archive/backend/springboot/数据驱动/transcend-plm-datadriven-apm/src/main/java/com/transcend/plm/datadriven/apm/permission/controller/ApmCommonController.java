package com.transcend.plm.datadriven.apm.permission.controller;

import cn.hutool.core.lang.tree.Tree;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.feign.model.qo.UserQueryRequest;
import com.transcend.plm.datadriven.apm.feign.model.vo.PlatFormUserDTO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmUserInfoBackupSyncAo;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmDepartmentBackupService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmUserInfoBackupService;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.request.UacRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/15 10:03
 * @Describe 敏捷项目管理-公共接口
 */
@RestController
@RequestMapping("/apm/common")
@Api(value = "ApmCommonController", tags = "敏捷项目管理-公共接口")
public class ApmCommonController {

    @Resource
    private IPlatformUserWrapper userWrapper;
    @Resource
    private ApmRoleService apmRoleService;
    @Resource
    private ApmDepartmentBackupService apmDepartmentBackupService;
    @Resource
    private ApmUserInfoBackupService apmUserInfoBackupService;

    @Value("${transcend.plm.apm.dept.three.defaultId:55999999}")
    private Long defaultDeptId;


    @PostMapping("/queryPlatformUser")
    @ApiOperation("查询用户中心用户接口")
    public PageDTO<PlatFormUserDTO> queryPlatformUser(@RequestBody UacRequest<UserQueryRequest> request) {
        return userWrapper.queryPlatformUser(request);
    }


    @GetMapping("/queryThreeDeptInfo")
    @ApiOperation("查询当前用户三级部门接口")
    public ThreeDeptVO queryThreeDeptInfo() {
        return apmRoleService.queryThreeDeptInfo(SsoHelper.getJobNumber());
    }

    @GetMapping("/queryThreeDeptInfo/{jobNumber}")
    @ApiOperation("查询当前用户三级部门接口,根据工号")
    public ThreeDeptVO queryThreeDeptInfo(@PathVariable String jobNumber) {
        return apmRoleService.queryThreeDeptInfo(jobNumber);
    }

    @GetMapping("/queryChildDept")
    @ApiOperation("查询当前用户三级部门接口,根据工号")
    public List<Tree<Long>> queryChildDept(@RequestParam(value = "deptId", required = false) Long deptId) {
        if (deptId == null) {
            deptId = defaultDeptId;
        }
        return userWrapper.queryChildDept(deptId);
    }

    @GetMapping("/department/backup")
    @ApiOperation("部门全量备份")
    public void departmentBackup(@RequestParam(value = "deptId", required = false) Long deptId) {
        if (deptId == null) {
            deptId = defaultDeptId;
        }
        apmDepartmentBackupService.syncDepartment(deptId);
    }

    @PostMapping("/userinfo/backup")
    @ApiOperation("用户信息备份")
    public void userinfoBackup(@RequestBody ApmUserInfoBackupSyncAo syncAo) {
        apmUserInfoBackupService.syncUserInfo(syncAo);
    }
}
