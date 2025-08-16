package com.transcend.plm.datadriven.apm.space.controller;

import cn.hutool.core.util.BooleanUtil;
import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.apm.mapstruct.ApmSpaceAppConverter;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppAo;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmSpaceRoleQO;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppCopyDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author yuanhu.huang <yuanhu.huang@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "APM SpaceManage Controller", tags = "空间配置-控制器")
@RequestMapping(value = "/apm/space-manage")
public class ApmSpaceManageController {

    @Autowired
    private ApmSpaceApplicationService apmSpaceApplicationService;

    @ApiOperation("新增空间")
    @PostMapping("/saveApmSpace")
    public TranscendApiResponse<Boolean> saveApmSpace(@RequestBody ApmSpaceDto apmSpaceDto) {
        return TranscendApiResponse.success(apmSpaceApplicationService.saveApmSpace(apmSpaceDto));
    }

    @ApiOperation("根据应用bid查询空间bid")
    @PostMapping("/getSpaceBySpaceAppBids")
    public TranscendApiResponse<Map<String, String>> getSpaceBySpaceAppBids(@RequestBody List<String> spaceAppBids) {
        return TranscendApiResponse.success(apmSpaceApplicationService.getSpaceBySpaceAppBids(spaceAppBids));
    }

    @ApiOperation("复制空间应用")
    @PostMapping("/copyApmSpaceApp")
    public TranscendApiResponse<Boolean> copyApmSpaceApp(@RequestBody ApmSpaceAppCopyDto apmSpaceAppCopyDto) {
        return TranscendApiResponse.success(apmSpaceApplicationService.copySpaceAppInfo(apmSpaceAppCopyDto.getOldSpaceAppBid(), apmSpaceAppCopyDto.getNewSpaceBid()));
    }

    @ApiOperation("修改空间")
    @PostMapping("/updateApmSpace")
    public TranscendApiResponse<Boolean> updateApmSpace(@RequestBody ApmSpaceDto apmSpaceDto) {
        return TranscendApiResponse.success(apmSpaceApplicationService.updateApmSpace(apmSpaceDto));
    }

    @ApiOperation("获取模板空间列表")
    @GetMapping("/listTemplate")
    public TranscendApiResponse<List<ApmSpaceVo>> listSpaceTemplate() {
        return TranscendApiResponse.success(apmSpaceApplicationService.listSpaceTemplate());
    }

    @ApiOperation("逻辑删除空间")
    @PostMapping("/logicDeleteApmSpace/{bid}")
    public TranscendApiResponse<Boolean> logicDeleteApmSpace(@ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.logicDeleteApmSpace(bid));
    }

    @ApiOperation("设置默认空间")
    @PostMapping("/setDefaultSpace/{bid}")
    public TranscendApiResponse<Boolean> setDefaultSpace(@ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.setDefaultSpace(bid));
    }


    @ApiOperation("设置为是否模板")
    @PostMapping("/{bid}/changeTemplate/{templateFlag}")
    public TranscendApiResponse<Boolean> changeTemplateSpace(@ApiParam("bid") @PathVariable("bid") String bid,
                                                             @ApiParam("模板标识（true：是模板，false：不是模板）")
                                                             @PathVariable("templateFlag") Boolean templateFlag) {
        return TranscendApiResponse.success(apmSpaceApplicationService.changeTemplateSpace(bid, templateFlag));
    }

    @ApiOperation("获取空间列表+应用")
    @GetMapping("/listApmSpaceAndApp")
    public TranscendApiResponse<List<ApmSpaceVo>> listApmSpaceAndApp() {
        return TranscendApiResponse.success(apmSpaceApplicationService.listApmSpace());
    }

    @ApiOperation("获取空间列表+应用")
    @PostMapping("/manage/listApmSpace")
    public TranscendApiResponse<List<ApmSpaceVo>> listApmSpaceManage(@RequestBody ApmSpaceQo query) {
        return TranscendApiResponse.success(apmSpaceApplicationService.listApmSpace(query));
    }

    /**
     * 获取空间列表+应用
     *
     * @return 响应
     * @deprecated 已废弃，因为使用listApmSpaceManage方法替代
     */
    @ApiOperation("获取空间列表+应用")
    @GetMapping("/listApmSpace")
    @Deprecated
    public TranscendApiResponse<List<ApmSpaceVo>> listApmSpace() {
        return TranscendApiResponse.success(apmSpaceApplicationService.listApmSpace());
    }

    @ApiOperation("获取空间列表")
    @PostMapping("/page")
    public TranscendApiResponse<PagedResult<ApmSpaceVo>> pageApmSpace(@RequestBody BaseRequest<ApmSpaceQo> pageRequest) {
        return TranscendApiResponse.success(apmSpaceApplicationService.pageApmSpace(pageRequest));
    }

    @ApiOperation("空间配置 - 查询角色&权限列表")
    @PostMapping("/listRoleAndPermission")
    public TranscendApiResponse<List<ApmRoleVO>> listRoleAndPermission(@RequestBody ApmSpaceRoleQO apmSpaceRoleQo) {
        return TranscendApiResponse.success(apmSpaceApplicationService.listRoleAndPermission(apmSpaceRoleQo));
    }

    @ApiOperation("空间配置 - 查询角色&系统角色列表")
    @PostMapping("/listRoleAndSystemRole")
    public TranscendApiResponse<List<ApmRoleVO>> listRoleAndSystemRole(@RequestBody ApmSpaceRoleQO apmSpaceRoleQo){
        return TranscendApiResponse.success(apmSpaceApplicationService.listRoleAndSystemRole(apmSpaceRoleQo));
    }

    @ApiOperation("空间配置 - 查询角色&权限列表通过域Bid")
    @GetMapping("/listRoleAndPermissionBySphereBid/{sphereBid}")
    public TranscendApiResponse<List<ApmRoleVO>> listRoleAndPermissionBySphereBid(@ApiParam("sphereBid") @PathVariable("sphereBid") String sphereBid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.listRoleAndPermissionBySphereBid(sphereBid));
    }

    @ApiOperation("空间配置 - 添加应用")
    @PostMapping("/addApp")
    public TranscendApiResponse<ApmSpaceApp> addApp(@RequestBody ApmSpaceAppAo apmSpaceAppAo) {
        return TranscendApiResponse.success(apmSpaceApplicationService.addApp(apmSpaceAppAo));
    }

    @ApiOperation("空间配置 - 查询应用列表")
    @GetMapping("/listApp/{spaceBid}")
    public TranscendApiResponse<List<ApmSpaceAppVo>> listApp(@ApiParam("spaceBid") @PathVariable("spaceBid") String spaceBid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.listApp(spaceBid));
    }

    @ApiOperation("空间配置 - 操作应用（删除、启用、禁用、修改）")
    @PostMapping("/operationApp")
    public TranscendApiResponse<Boolean> operationApp(@RequestBody ApmSpaceAppAo apmSpaceAppAo) {
        Boolean operationResult = apmSpaceApplicationService.operationApp(apmSpaceAppAo);
        // 放止拆包空指针
        return TranscendApiResponse.success(BooleanUtil.isTrue(operationResult));
    }

    @ApiOperation("空间配置 - 空间应用排序")
    @PostMapping("/sortApp")
    public TranscendApiResponse<Boolean> sortApp(@RequestBody List<ApmSpaceAppAo> apmSpaceAppAoList) {
        return TranscendApiResponse.success(apmSpaceApplicationService.sortApp(apmSpaceAppAoList));
    }

    /**
     * 查询当前登陆人对当前空间的权限信息
     */
    @GetMapping("/getPermissionBySpaceBid/{sphereBid}")
    @ApiOperation("查询当前登陆人对当前空间的权限信息")
    public TranscendApiResponse<List<ApmSpaceAppAccessVo>> getPermissionBySphereBid(@ApiParam("sphereBid") @PathVariable("sphereBid") String sphereBid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.getPermissionBySphereBid(sphereBid));
    }

    @ApiOperation("实例团队查询")
    @GetMapping("/queryOrCreateTeam/{spaceBid}/{spaceAppBid}/{bid}")
    public TranscendApiResponse<List<ApmRoleVO>> queryOrCreateTeam(@PathVariable("spaceBid") String spaceBid,
                                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                                   @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceApplicationService.queryOrCreateTeam(spaceBid, spaceAppBid, bid));
    }

    @ApiOperation("根据bid查询应用信息")
    @GetMapping("/getSpaceAppByBid/{bid}")
    public TranscendApiResponse<ApmSpaceAppVo> getSpaceAppByBid(@PathVariable("bid") String bid) {
        ApmSpaceApp apmSpaceApp = apmSpaceApplicationService.getSpaceAppByBid(bid);
        return TranscendApiResponse.success(ApmSpaceAppConverter.INSTANCE.po2vo(apmSpaceApp));
    }
}
