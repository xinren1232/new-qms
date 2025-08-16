package com.transcend.plm.datadriven.apm.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.api.feign.ApmUserDemandFeignClient;
import com.transcend.plm.datadriven.apm.api.service.UserDemandService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "API-用户需求客制化-控制器")
public class UserDemandController implements ApmUserDemandFeignClient {

    @Resource
    private UserDemandService userDemandService;


    @ApiOperation("批量新增")
    @Override
    public TranscendApiResponse<Boolean> batchAdd(List<Map<String, Object>> dataList) {
        return TranscendApiResponse.success(userDemandService.batchAdd(dataList));
    }

    @ApiOperation("新增")
    @Override
    public TranscendApiResponse<Boolean> add(Map<String, Object> param) {
        return TranscendApiResponse.success(userDemandService.add(param));
    }

    @ApiOperation("修改")
    @Override
    public TranscendApiResponse<Boolean> update(Map<String, Object> param) {
        return TranscendApiResponse.success(userDemandService.update(param));
    }

    /**
     *
     * @param
     * @return
     */
    @ApiOperation("批量逻辑删除")
    @Override
    public TranscendApiResponse<Boolean> batchLogicalDeleteDelete(List<String> bids) {
        return TranscendApiResponse.success(userDemandService.batchLogicalDeleteDelete(bids));
    }

}
