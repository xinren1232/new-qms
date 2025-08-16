package com.transcend.plm.datadriven.apm.flow.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowTemplateAO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowApplicationService;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateNodeVO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang <yuanhu.huang@transsion.com>
 * @version V1.0.0
 * @date 2023/9/28 10:07
 * @since 1.0
 **/
@RestController
@Api(value = "ApmFlowController", tags = "流程-控制器")
@RequestMapping(value ="/apm/flow")
public class ApmFlowController {

    @Autowired
    private ApmFlowApplicationService apmFlowApplicationService;

    @ApiOperation("新增或者修改流程模板")
    @PostMapping("/saveOrUpdate")
    public TranscendApiResponse<ApmFlowTemplateVO> saveOrUpdate(@RequestBody ApmFlowTemplateAO apmFlowTemplateAO){
       return TranscendApiResponse.success(apmFlowApplicationService.saveOrUpdate(apmFlowTemplateAO));
    }
    @ApiOperation("根据模板bid查询模板明细信息")
    @GetMapping("/get/{bid}")
    public TranscendApiResponse<ApmFlowTemplateVO> getBid(@ApiParam("bid") @PathVariable("bid") String bid){
        return TranscendApiResponse.success(apmFlowApplicationService.getByBid(bid));
    }
    @ApiOperation("根据模板bid查询模板明细信息")
    @GetMapping("/listBySpaceAppBid/{spaceAppBid}")
    public TranscendApiResponse<List<ApmFlowTemplateVO>> listBySpaceAppBid(@ApiParam("spaceAppBid") @PathVariable("spaceAppBid") String spaceAppBid){
        return TranscendApiResponse.success(apmFlowApplicationService.listBySpaceAppBid(spaceAppBid));
    }

    @ApiOperation("根据模板bid查询模板明细信息,过滤掉状态流程")
    @GetMapping("/listBySpaceAppBidNoState/{spaceAppBid}")
    public TranscendApiResponse<List<ApmFlowTemplateVO>> listBySpaceAppBidNoState(@ApiParam("spaceAppBid") @PathVariable("spaceAppBid") String spaceAppBid){
        List<ApmFlowTemplateVO> apmFlowTemplateVOS = apmFlowApplicationService.listBySpaceAppBid(spaceAppBid);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateVOS)){
            for(int i = apmFlowTemplateVOS.size()-1;i>=0;i--){
                if("state".equals(apmFlowTemplateVOS.get(i).getType())){
                    apmFlowTemplateVOS.remove(i);
                }
            }
        }
        return TranscendApiResponse.success(apmFlowTemplateVOS);
    }

    @ApiOperation("根据模板bid查询节点信息")
    @GetMapping("/template/nodes/{templateBid}")
    public TranscendApiResponse<List<ApmFlowTemplateNodeVO>> listNodesByTemplateBid(@ApiParam("templateBid") @PathVariable("templateBid") String templateBid){
        return TranscendApiResponse.success(apmFlowApplicationService.listNodesByTemplateBid(templateBid));
    }

    @ApiOperation("根据模板bid查询模板明细信息")
    @GetMapping("/listBySpaceBidAndModelCode/{spaceBid}/{modelCode}")
    public TranscendApiResponse<List<ApmFlowTemplateVO>> listBySpaceAppBid(@ApiParam("spaceBid") @PathVariable("spaceBid") String spaceBid,@ApiParam("modelCode") @PathVariable("modelCode") String modelCode){
        return TranscendApiResponse.success(apmFlowApplicationService.listBySpaceBidAndModelCode(spaceBid,modelCode));
    }

    @ApiOperation("根据模板bid查询节点信息")
    @GetMapping("/listNodes/{templateBid}")
    public TranscendApiResponse<String> getNodeVos(@ApiParam("templateBid") @PathVariable("templateBid") String templateBid){
        return TranscendApiResponse.success(apmFlowApplicationService.getTemplateLayout(templateBid));
    }
    @ApiOperation("根据节点bid查询节点明细信息")
    @GetMapping("/getNodeDetail/{nodeBid}")
    public TranscendApiResponse<ApmFlowTemplateNodeVO> getNodeDetail(@ApiParam("nodeBid") @PathVariable("nodeBid") String nodeBid){
        return TranscendApiResponse.success(apmFlowApplicationService.getNodeDetail(nodeBid));
    }
    @ApiOperation("查询模板的所以角色人员信息")
    @GetMapping("/listFlowTemplateRoles/{flowTemplateBid}")
    public TranscendApiResponse<List<ApmRoleAndIdentityVo>> listFlowTemplateRoles(@ApiParam("flowTemplateBid") @PathVariable("flowTemplateBid") String flowTemplateBid){
        return TranscendApiResponse.success(apmFlowApplicationService.listFlowTemplateRoles(flowTemplateBid));
    }

    @ApiOperation("查询模板的所以角色人员信息")
    @PostMapping("/delete/{flowTemplateBid}")
    public TranscendApiResponse<Boolean> delete(@ApiParam("flowTemplateBid") @PathVariable("flowTemplateBid") String flowTemplateBid){
        return TranscendApiResponse.success(apmFlowApplicationService.delete(flowTemplateBid));
    }

    @Resource(name="lifeCycleCodeResetCustomizeService")
    IFlowCustomizeMethod flowCustomizeMethodl;
    @GetMapping("/test")
    public TranscendApiResponse<String> test(){
        FlowEventBO flowEventBO = new FlowEventBO();
        ApmFlowInstanceNode apmFlowInstanceNode = new ApmFlowInstanceNode();
        apmFlowInstanceNode.setSpaceAppBid("1253656300279398400");
        apmFlowInstanceNode.setInstanceBid("1257334540684484608");
        flowEventBO.setInstanceNode(apmFlowInstanceNode);
        flowCustomizeMethodl.execute(flowEventBO);
        return TranscendApiResponse.success("test");
    }


}
