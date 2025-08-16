package com.transcend.plm.datadriven.core.action;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.api.model.mata.RelationCrossUpApplicationQo;
import com.transcend.plm.datadriven.api.model.relation.qo.CrossRelationPathChainQO;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationQo;
import com.transcend.plm.datadriven.api.model.relation.vo.RelationAndTargetVo;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.domain.object.relation.RelationObjectManageService;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 关系对象控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/18 17:07
 */
@Api(value = "RelationObjectController", tags = "数据驱动-关系对象-控制器")
@Validated
@RestController
@RequestMapping("/data-driven/api/relation-object")
public class RelationObjectController {
    @Resource
    private RelationObjectManageService relationObjectManageService;


    /**
     * @param modelCode
     * @param dataBid
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("新增基本信息")
    @PostMapping("/{modelCode}/checkOut/{dataBid}")
    public TranscendApiResponse<MObject> add(@ApiParam("模型编码") @PathVariable String modelCode,
                                             @ApiParam("dataBid") @PathVariable String dataBid) {
        return null;

    }

    @ApiOperation("生命周期状态提升")
    @PostMapping("/promote")
    public TranscendApiResponse<MVersionObject> promote(@RequestBody LifeCyclePromoteDto dto) {
        return TranscendApiResponse.success(relationObjectManageService.promote(dto));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MVersionObject }>
     */
    @ApiOperation("修订")
    @PostMapping("/{modelCode}/revise/{bid}")
    public TranscendApiResponse<MVersionObject> revise(@ApiParam("模型编码") @PathVariable String modelCode,
                                                       @ApiParam("bid") @PathVariable String bid) {
        return TranscendApiResponse.success(relationObjectManageService.revise(ReviseDto.builder().bid(bid).modelCode(modelCode).build()));
    }

    /**
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("测试")
    @GetMapping("/test")
    public TranscendApiResponse<Boolean> test() {
        return TranscendApiResponse.success(true);
    }

    /**
     * 分页查询关系对象和目标对象实例数据
     *
     * @param pageQo
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link RelationAndTargetVo }>>
     */
    @ApiOperation("分页查询关系对象和目标对象实例数据")
    @PostMapping("/pageRelationAndTarget")
    public TranscendApiResponse<PagedResult<RelationAndTargetVo>> pageRelationAndTarget(@RequestBody BaseRequest<RelationQo> pageQo) {
        return TranscendApiResponse.success(relationObjectManageService.pageRelationAndTarget(pageQo));
    }

    /**
     * 自下向上寻址的跨层级查询源对象的实例集合 强浮动以bid作为关联
     *
     * @param qo
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link MObject }>>
     */
    @ApiOperation("自下向上寻址的跨层级查询源对象的实例集合 强浮动以bid作为关联")
    @PostMapping("/pageCrossHierarchyUp")
    public TranscendApiResponse<PagedResult<MObject>> pageCrossHierarchyUp(@RequestBody RelationCrossUpApplicationQo qo) {

        return TranscendApiResponse.success(
                relationObjectManageService.pageCrossHierarchyUp(
                        qo, QueryConveterTool.convert(qo.getPageQo())
                ));
    }

    /**
     * 跨层级查询源对象的实例集合 强浮动以bid作为关联
     *
     * @param qo
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link MObject }>>
     */
    @ApiOperation("跨层级查询源对象的实例集合 强浮动以bid作为关联")
    @PostMapping("/pageCrossRelationInstance")
    public TranscendApiResponse<PagedResult<MObject>> pageCrossRelationInstance(@RequestBody @Validated CrossRelationPathChainQO qo) {

        return TranscendApiResponse.success(
                relationObjectManageService.pageCrossRelationInstance(
                        qo, QueryConveterTool.convert(qo.getPageQo())
                ));
    }



}
