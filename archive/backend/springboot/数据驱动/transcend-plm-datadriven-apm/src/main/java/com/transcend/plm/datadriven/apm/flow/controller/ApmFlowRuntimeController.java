package com.transcend.plm.datadriven.apm.flow.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowUserDto;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceNodeVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceProcessVo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceVO;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程运行时Controller
 * @createTime 2023-10-08 10:20:00
 */
@RestController
@Api(value = "ApmFlowRuntimeController", tags = "流程运行时-控制器")
@RequestMapping(value = "/apm/flow/runtime")
public class ApmFlowRuntimeController {
    @Resource
    private IRuntimeService runtimeService;

    @GetMapping(value = "/listInstanceNodes/{instanceBid}")
    @ApiOperation("查询流程状态")
    public ApmFlowInstanceVO listInstanceNodes(@PathVariable String instanceBid) {
        return runtimeService.listInstanceNodes(instanceBid);
    }

    @PostMapping(value = "/listNodes")
    @ApiOperation("查询流程状态")
    public ApmFlowInstanceVO listInstanceNodesByApmStateQo(@RequestBody ApmStateQo apmStateQo) {
        return runtimeService.listInstanceNodesByApmStateQo(apmStateQo);
    }


    @PostMapping(value = "/listKeyLifeCycleCodes")
    @ApiOperation("查询关键生命周期")
    public List<ApmFlowTemplateNode> listKeyLifeCycleCodes(@RequestBody ApmStateQo apmStateQo) {
        return runtimeService.listKeyLifeCycleCodes(apmStateQo);
    }


    /**
     * 生成一个发起流程的方法
     *
     * @param templateBid 模板id
     * @param instanceBid 实例id
     * @param qo          流程参数
     * @return 流程实例id
     */
    @PostMapping(value = "/startProcess/{templateBid}/{instanceBid}")
    @ApiOperation("发起流程")
    public Boolean startProcess(@PathVariable String templateBid, @PathVariable String instanceBid, @RequestBody ApmFlowQo qo) {
        return runtimeService.startProcess(templateBid, instanceBid, qo, null);
    }

    @PostMapping(value = "/completeNode/{nodeBid}")
    @ApiOperation("完成节点任务")
    public Boolean completeNode(@PathVariable String nodeBid) {
        return runtimeService.completeNode(nodeBid, null);
    }

    /**
     * 生成一个回退节点的方法
     *
     * @param nodeBid 节点id
     * @return 流程实例id
     */
    @PostMapping(value = "/rollback/{nodeBid}")
    @ApiOperation("回退节点任务")
    public Boolean rollback(@PathVariable String nodeBid) {
        return runtimeService.rollback(nodeBid, true);
    }

    /**
     * 生成一个回退节点的方法
     *
     * @param instanceBid    流程实例id
     * @return 流程实例id
     */
    @GetMapping(value = "/listRollbackNode/{instanceBid}")
    @ApiOperation("回退节点集合")
    public List<ApmFlowInstanceNodeVO> listRollbackNode(@PathVariable String instanceBid) {
        return runtimeService.listRollbackNode(instanceBid);
    }

    /**
     * 生成一个查询角色用户的方法
     *
     * @param nodeBid 节点id
     * @return 流程实例id
     */
    @GetMapping(value = "/listRoleUser/{nodeBid}")
    @ApiOperation("查询角色用户")
    public List<ApmRoleUserAO> listRoleUser(@PathVariable String nodeBid) {
        return runtimeService.listNodeRoleUser(nodeBid);
    }

    @GetMapping(value = "/listLifeCycleCodeRoleUser/{instanceBid}/{lifeCycleCode}")
    @ApiOperation("查询角色用户")
    public List<ApmRoleUserAO> listLifeCycleCodeRoleUser(@PathVariable String instanceBid, @PathVariable String lifeCycleCode) {
        return runtimeService.listLifeCycleCodeRoleUser(lifeCycleCode, instanceBid);
    }

    /**
     * 修改实例角色绑定的人员
     *
     * @param instanceBid 实例id
     * @param qo          qo
     * @return Boolean
     */
    @PostMapping(value = "/updateRoleUser/{instanceBid}")
    @ApiOperation("修改实例角色绑定的人员")
    public Boolean updateRoleUser(@PathVariable String instanceBid, @RequestBody ApmFlowQo qo) {
        return runtimeService.updateRoleUser(instanceBid, qo);
    }

    @PostMapping(value = "/updateRoleUserByLifeCycleCode/{instanceBid}/{lifeCycleCode}")
    @ApiOperation("修改实例角色绑定的人员")
    public Boolean updateRoleUser(@PathVariable String instanceBid, @PathVariable String lifeCycleCode, @RequestBody List<ApmRoleUserAO> roleUserAOList) {
        return runtimeService.updateRoleUserByLifeCycleCode(instanceBid, lifeCycleCode, roleUserAOList);
    }

    /**
     * 修改实例角色绑定的人员
     *
     * @param instanceBid 实例id
     * @param qo          qo
     * @return Boolean
     */
    @PostMapping(value = "/updateRoleUserByCode/{instanceBid}/{roleCode}")
    @ApiOperation("修改实例角色绑定的人员")
    public Boolean updateRoleUserByCode(@PathVariable String instanceBid, @PathVariable String roleCode, @RequestBody ApmFlowUserDto qo) {
        return runtimeService.saveFlowRoleUsers(instanceBid, qo.getRoleUserList(), qo.getSpaceBid(), qo.getSpaceAppBid(), roleCode);
    }

    /**
     * 查询实例绑定的角色用户
     *
     * @param instanceBid 实例id
     * @return List<ApmRoleUserAO>
     */
    @GetMapping(value = "/listInstanceRoleUser/{instanceBid}")
    @ApiOperation("查询实例绑定的角色用户")
    public List<ApmRoleUserAO> listInstanceRoleUser(@PathVariable String instanceBid) {
        return runtimeService.listInstanceRoleUser(instanceBid);
    }

    /**
     * 查询流程审批历程
     *
     * @param instanceBid 实例id
     * @return List<ApmFlowInstanceProcessVo>
     */
    @GetMapping(value = "/listInstanceProcess/{instanceBid}")
    @ApiOperation("查询流程审批历程")
    public List<ApmFlowInstanceProcessVo> listInstanceProcess(@PathVariable String instanceBid) {
        return runtimeService.listInstanceProcess(instanceBid);
    }

    /**
     * 删除流程实例
     *
     * @param instanceBid 实例id
     * @return Boolean
     */
    @PostMapping(value = "/deleteProcess/{instanceBid}")
    @ApiOperation("删除流程实例")
    public Boolean deleteProcess(@PathVariable String instanceBid) {
        return runtimeService.deleteProcess(instanceBid);
    }

    /**
     * 批量删除流程实例
     *
     * @param instanceBidList 实例id列表
     * @return Boolean
     */
    @PostMapping(value = "/batchDeleteProcess")
    @ApiOperation("批量删除流程实例")
    public Boolean batchDeleteProcess(@RequestBody List<String> instanceBidList) {
        return runtimeService.deleteProcess(instanceBidList);
    }

    @PostMapping(value = "/completeNodeForce/{nodeBid}")
    @ApiOperation("强制完成节点任务")
    public Boolean completeNodeForce(@PathVariable String nodeBid) {
        return runtimeService.completeNodeForce(nodeBid);
    }


    @PostMapping(value = "/updateDemandScheduleRoleUserByModuleBid/{instanceBid}/{moduleBid}")
    @ApiOperation("修改模块更新需求排期节点责任人")
    public TranscendApiResponse<Map<String, Object>> updateDemandScheduleRoleUserByModuleBid(@PathVariable String instanceBid, @PathVariable String moduleBid) {
        return TranscendApiResponse.success(runtimeService.updateDemandScheduleRoleUserByModuleBid(instanceBid, moduleBid));
    }

}
