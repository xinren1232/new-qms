package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.entity.ao.*;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.powerjob.notify.SyncDutyFieldInstance;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 需求管理定制业务控制器
 * @date 2024/06/21 11:03
 **/
@Api(value = "Demand Management Controller", tags = "空间-需求管理-控制器")
@RestController
@RequestMapping(value = ("/alm/demandManager/{spaceBid}/app/{spaceAppBid}"))
public class DemandManagementController {
    @Resource
    private DemandManagementService demandManagementService;
    @Resource
    private RuntimeService runtimeService;

    @Resource
    private SyncDutyFieldInstance syncDutyFieldInstance;
    /**
     * @description 检查需求是否可以设置为重复需求
     * @param instanceId 实例数据id
     **/
    @GetMapping("/checkRepeat/{instanceId}")
    public TranscendApiResponse<Boolean> checkRepeat(@PathVariable("spaceBid") String spaceBid,
                                                     @PathVariable("spaceAppBid") String relationSpaceAppBid,
                                                     @PathVariable("instanceId") String instanceId) {
        return TranscendApiResponse.success(demandManagementService.checkRepeat(spaceBid, relationSpaceAppBid, instanceId));
    }

    @ApiOperation("查询RR下的领域/应用/模块树数据")
    @GetMapping("/queryDomainTree/{rrBid}")
    public TranscendApiResponse<List<MObject>> queryDomainTree(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid,
                                                                     @ApiParam("RR需bid") @PathVariable String rrBid) {
        return TranscendApiResponse.success(demandManagementService.queryDomainTree(spaceBid, rrBid));
    }

    @ApiOperation("查询领域选择下拉框数据")
    @GetMapping({"/queryDomainSelection/{type}/{rrBid}", "/queryDomainSelection/{type}/{rrBid}/{selectedBid}"})
    public TranscendApiResponse<List<SelectVo>> queryDomainSelection(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid,
                                                                     @ApiParam("下拉框数据类型 1:小模块选择 2:领域/模块/应用选择") @PathVariable Integer type,
                                                                     @ApiParam("RR需求关联领域编码") @PathVariable(required = false) String rrBid,
                                                                     @ApiParam("type = 2时选中某条数据的bid") @PathVariable(required = false) String selectedBid) {
        return TranscendApiResponse.success(demandManagementService.queryDomainSelection(spaceBid, type, rrBid, selectedBid));
    }

    @ApiOperation("RR需求选取领域")
    @PostMapping("/{rrBid}/selectDomain/{type}")
    public TranscendApiResponse<List<MObject>> selectDomain(@PathVariable("spaceBid") String spaceBid,
                                                            @PathVariable("spaceAppBid") String spaceAppBid,
                                                            @ApiParam("RR需求BID") @PathVariable("rrBid") String rrBid,
                                                            @ApiParam("下拉框数据类型 1:小模块选择 2:领域/模块/应用选择") @PathVariable Integer type
            , @RequestBody SelectAo selectAo) {
        return TranscendApiResponse.success(demandManagementService.selectAllDomain(spaceBid, spaceAppBid,rrBid, selectAo,null,null) );
    }

    @ApiOperation("复制领域")
    @PostMapping("/{rrBid}/copyDomain")
    public TranscendApiResponse<Boolean> copyDomain(@PathVariable("spaceBid") String spaceBid,
                                                            @PathVariable("spaceAppBid") String spaceAppBid,
                                                            @ApiParam("RR需求BID") @PathVariable("rrBid") String rrBid,
                                                            @RequestBody MObjectCopyAo sourceParam) {
        return TranscendApiResponse.success(demandManagementService.copyDomain(spaceBid, spaceAppBid,rrBid, sourceParam) );
    }

    @ApiOperation("RR需求移除领域/应用/模块")
    @PostMapping("/batchRemoveDomain")
    public TranscendApiResponse<Boolean> batchRemoveDomain(@PathVariable("spaceBid") String spaceBid,
                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                      @RequestBody RemoveDomainAo removeDomainAo) {
        return TranscendApiResponse.success(demandManagementService.batchRemoveDomain(spaceBid, spaceAppBid, removeDomainAo));
    }

    @ApiOperation("RR需求更新主导领域")
    @PostMapping("/updateLeadDomain")
    public TranscendApiResponse<Boolean> updateLeadDomain(@PathVariable("spaceBid") String spaceBid,
                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                          @RequestBody UpdateLeadDomainAo updateLeadDomainAo) {
        return TranscendApiResponse.success(demandManagementService.updateLeadDomain(spaceBid, spaceAppBid, updateLeadDomainAo));
    }

    @ApiOperation("RR需求更新责任人")
    @PostMapping("/updateResponsiblePerson")
    public TranscendApiResponse<Boolean> updateResponsiblePerson(@PathVariable("spaceBid") String spaceBid,
                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                          @RequestBody UpdateResponsibleAo updateResponsibleAo) {
        return TranscendApiResponse.success(demandManagementService.updateResponsiblePerson(spaceBid, spaceAppBid, updateResponsibleAo));
    }
    @ApiOperation("领域组件 - 新增IR需求")
    @PostMapping("/addIR")
    public TranscendApiResponse<Boolean> addIR(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @Valid @RequestBody IRAddAndRelateAo param
    ) {
        MSpaceAppData irData = param.getRelationAddParam().getTargetMObjects().get(0);
        Boolean result = demandManagementService.addIR(spaceBid, spaceAppBid, param);
        if(irData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null){
            CompletableFuture.runAsync(() -> runtimeService.runStartNode(irData.getBid(),spaceBid,spaceAppBid), SimpleThreadPool.getInstance());
        }
        return TranscendApiResponse.success(result);
    }

    @ApiOperation("领域组件 - 关联IR需求")
    @PostMapping("/relateIR")
    public TranscendApiResponse<Boolean> relateIR(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @Valid @RequestBody IRAddAndRelateAo param
    ) {
        return TranscendApiResponse.success(demandManagementService.relateIR(spaceBid, spaceAppBid, param));
    }

    @ApiOperation("领域组件 - 移除IR需求")
    @PostMapping("/removeIR")
    public TranscendApiResponse<Boolean> removeIR(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @Valid @RequestBody IRRemAo param
    ) {
        return TranscendApiResponse.success(demandManagementService.removeIR(spaceBid, spaceAppBid, param));
    }


    @ApiOperation("获取雪花ID")
    @GetMapping("/getSnowBid")
    public TranscendApiResponse<String> getSnowBid() {
        return TranscendApiResponse.success(SnowflakeIdWorker.nextIdStr());
    }


    @ApiOperation("获取雪花ID")
    @GetMapping("/aaa")
    public TranscendApiResponse<String> aaa() {
         syncDutyFieldInstance.syncInstance();
         return null;
    }



}
