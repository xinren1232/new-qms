package com.transcend.plm.datadriven.apm.space.controller;

import com.transsion.framework.common.StringUtil;
import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.assertj.core.util.Sets;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2025/4/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataDrivenController", tags = "跨越-空间-应用配置驱动-控制器")
@RequestMapping(value = "/apm/cross-space/data-driven")
public class ApmCrossSpaceDataDrivenController {

    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;

    @ApiOperation("跨空间-tab下的表格视图-选择列表（仅支持编号+名称查询）")
    @PostMapping("/tab/table-view/{modelCode}/select-page")
    public TranscendApiResponse<PagedResult<MObject>> tabTableViewSelectPage(
            @ApiParam("模型编码") @PathVariable("modelCode") String modelCode,
            @ApiParam("分页查询参数") @RequestBody BaseRequest<ModelMixQo> pageQo) {

        ModelMixQo param = pageQo.getParam();

        // 后边优化
        String generalSearch = param.getGeneralSearch();

        // 结果字段
        Set<String> fields = Sets.newHashSet();
        fields.add( TranscendModelBaseFields.NAME);
        fields.add( TranscendModelBaseFields.CODING);
        fields.add( TranscendModelBaseFields.BID);
        fields.add( TranscendModelBaseFields.DATA_BID);
        fields.add( TranscendModelBaseFields.SPACE_BID);
        fields.add( TranscendModelBaseFields.SPACE_APP_BID);

        BaseRequest<QueryCondition> queryConditionBaseRequest = QueryConveterTool.convertFitterNullValue(pageQo);

        // 填充generalSearch过滤条件
        if (StringUtil.isNotBlank(generalSearch)) {
            queryConditionBaseRequest.getParam().setQueries(
                    QueryConveterTool.appendGeneralSearchQueriesAndCondition(queryConditionBaseRequest.getParam().getQueries(), generalSearch)
            );
        }
        // 结果集
        return TranscendApiResponse.success(objectModelCrudI.page(modelCode, queryConditionBaseRequest, true, fields));
    }
//
//    @ApiOperation("跨空间-tab下的表格视图-选择新增（需要判断权限是否可以，否则抛异常错误）")
//    @PostMapping("/tab/table-view/select-add")
//    public TranscendApiResponse<List<AppViewModelDto>> tabTableViewSelectAdd(@PathVariable("spaceBid") String spaceBid,
//                                                                          @PathVariable("spaceAppBid") String spaceAppBid) {
//        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.listViewModel(spaceAppBid));
//    }




}
