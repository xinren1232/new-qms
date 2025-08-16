package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.model.ao.FileActionAo;
import com.transsion.framework.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/9
 */
@FeignClient(name = "${transsion.ipm-integration.feign.name:ipm-integration}", url = "${transsion.ipm-integration.feign.url:}")
public interface IntegrationFeignClient {

    /**
     * filePostRecordAdd
     *
     * @param vo vo
     * @return {@link BaseResponse<Boolean>}
     */
    @PostMapping(value = "/file/manager/filePostRecordAdd", headers = {"appSecret=643a0a39842311ee886fc03c5906413a643a2baf842311eeba92c03c5906413a"})
    BaseResponse<Boolean> filePostRecordAdd(@RequestBody FileActionAo vo);

    /**
     * batchExecute
     *
     * @param recordBids recordBids
     * @return {@link BaseResponse<Boolean>}
     */
    @PostMapping("/file/manager/batchExecute")
    BaseResponse<Boolean> batchExecute(@RequestBody List<String> recordBids);

}
