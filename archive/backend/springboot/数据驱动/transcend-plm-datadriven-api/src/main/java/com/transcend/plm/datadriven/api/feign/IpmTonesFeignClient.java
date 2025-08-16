package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.model.GetEnumRequest;
import com.transcend.plm.datadriven.api.model.dto.CheckBoxDTO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author weixin.wang
 */
@FeignClient(name = "${transsion.ipm-tones.feign.name:service-ipm-tones}", url = "${transsion.ipm-tones.feign.url:}")
public interface IpmTonesFeignClient {

    /**
     * getCommonCheckBox
     *
     * @param request request
     * @return {@link ResponseVO<List<CheckBoxDTO>>}
     */
    @ApiOperation("获取公用枚举信息")
    @PostMapping(value = "/ipm-tones/project/info/getCommonCheckBox")
    ResponseVO<List<CheckBoxDTO>> getCommonCheckBox(@RequestBody GetEnumRequest request);
}
