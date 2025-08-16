package com.transcend.plm.alm.demandmanagement.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.alm.api.feign.SystemRequirementFeignClient;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.mapstruct.SimpleSrDtoConverter;
import com.transcend.plm.alm.model.ao.QuerySimpleListAO;
import com.transcend.plm.alm.model.dto.SimpleSrDTO;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.dto.BaseResponse;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SR对外输出api
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/20 14:51
 */
@AllArgsConstructor
@RestController
@Api(value = "SystemRequirementService", tags = "API-用户需求客制化-控制器")
public class SystemRequirementServiceImpl implements SystemRequirementFeignClient {

    private final ObjectModelStandardI<MObject> objectModelCrudI;

    @Override
    public BaseResponse<List<SimpleSrDTO>> simpleList(String searchKey) {

        //设置条件
        QueryCondition condition = QueryCondition.of()
                .setOrders(ListUtil.toList(Order.of().setProperty(ObjectEnum.CODING.getCode()).setDesc(true)));
        QueryWrapper wrapper = new QueryWrapper()
                .eq(BaseDataEnum.DELETE_FLAG.getCode(), 0).and().isNotNull(ObjectEnum.CODING.getCode());

        if (StringUtils.isNotBlank(searchKey)) {
            wrapper.and().addQueryWrapper(
                    new QueryWrapper().like(ObjectEnum.CODING.getCode(), searchKey)
            );
        }

        condition.setQueries(QueryWrapper.buildSqlQo(wrapper));

        BaseRequest<QueryCondition> request = new BaseRequest<>();
        request.setParam(condition);
        request.setSize(200);

        return BaseResponse.success(simpleSrQuery(request));
    }


    @Override
    public BaseResponse<List<SimpleSrDTO>> querySimpleList(QuerySimpleListAO ao) {
        //设置条件
        QueryCondition condition = QueryCondition.of()
                .setOrders(ListUtil.toList(Order.of().setProperty(ObjectEnum.CODING.getCode()).setDesc(true)));
        QueryWrapper wrapper = new QueryWrapper()
                .eq(BaseDataEnum.DELETE_FLAG.getCode(), 0).and().isNotNull(ObjectEnum.CODING.getCode());

        if (CollUtil.isNotEmpty(ao.getCodeList())) {
            wrapper.and().addQueryWrapper(
                    new QueryWrapper().in(ObjectEnum.CODING.getCode(), ao.getCodeList())
            );
        }

        condition.setQueries(QueryWrapper.buildSqlQo(wrapper));

        BaseRequest<QueryCondition> request = new BaseRequest<>();
        request.setParam(condition);
        request.setSize(Optional.ofNullable(ao.getReturnSize()).map(Math::abs).orElse(200));

        return BaseResponse.success(simpleSrQuery(request));
    }


    /**
     * 查询SR列表
     *
     * @param request 请求
     * @return 结果
     */
    private List<SimpleSrDTO> simpleSrQuery(BaseRequest<QueryCondition> request) {
        Set<String> fields = new HashSet<>();
        fields.add(BaseDataEnum.BID.getCode());
        fields.add(ObjectEnum.CODING.getCode());
        fields.add(ObjectEnum.NAME.getCode());

        //执行查询
        PagedResult<MObject> page = objectModelCrudI.page(TranscendModel.SR.getCode(), request, true, fields);

        //转换结果并返回
        return SimpleSrDtoConverter.INSTANCE.toDtoList(page.getData());
    }
}
