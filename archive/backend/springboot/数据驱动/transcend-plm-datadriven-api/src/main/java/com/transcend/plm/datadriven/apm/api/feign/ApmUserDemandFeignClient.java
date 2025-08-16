package com.transcend.plm.datadriven.apm.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.api.feign.fallback.DatadrivenClientFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author jie.luo1d
 * @Description: 配置对象feign
 * @date 2023年5月9日
 */
@FeignClient(name = "${transcend.datadriven.feign.name:transcend-plm-datadriven-apm}", url = "${transcend.datadriven.feign.url:}",
        fallbackFactory = DatadrivenClientFactory.class)
public interface ApmUserDemandFeignClient {

    /**
     * add
     *
     * @param param param
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("新增或者修改")
    @PostMapping("/api/apm/space/user-demand/add")
    TranscendApiResponse<Boolean> add(@RequestBody Map<String, Object> param);

    /**
     * update
     *
     * @param param param
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("修改")
    @PostMapping("/api/apm/space/user-demand/update")
    TranscendApiResponse<Boolean> update(@RequestBody Map<String, Object> param);

    /**
     * batchAdd
     *
     * @param dataList dataList
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("批量新增")
    @PostMapping("/api/apm/space/user-demand/batchAdd")
    TranscendApiResponse<Boolean> batchAdd(@RequestBody List<Map<String, Object>> dataList);

    /**
     * batchLogicalDeleteDelete
     *
     * @param bids bids
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("批量逻辑删除")
    @PostMapping("/api/apm/space/user-demand/batchLogicalDeleteDelete")
    TranscendApiResponse<Boolean> batchLogicalDeleteDelete(@RequestBody List<String> bids);

}
