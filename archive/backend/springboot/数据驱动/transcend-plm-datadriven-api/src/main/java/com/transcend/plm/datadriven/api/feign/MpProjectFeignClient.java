package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.model.qo.ProjectQo;
import com.transcend.plm.datadriven.api.model.vo.DictViewVO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author weixin.wang
 */
@FeignClient(name = "${transsion.mp-project.feign.name:service-mp-project}", url = "${transsion.mp-project.feign.url:}")
public interface MpProjectFeignClient {
    /**
     * queryAllProjectName
     *
     * @return {@link ResponseVO<List<DictViewVO>>}
     */
    @ApiOperation("查询所有未取消的项目 提供RR需求新增")
    @GetMapping("/mp-project/queryAllProjectName")
    ResponseVO<List<DictViewVO>> queryAllProjectName();
}
