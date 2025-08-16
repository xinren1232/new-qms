package com.transcend.plm.alm.api.feign;

import com.transcend.plm.alm.model.ao.QuerySimpleListAO;
import com.transcend.plm.alm.model.dto.SimpleSrDTO;
import com.transsion.framework.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * SR外部调用接口
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/20 11:51
 */
@FeignClient(name = "${transcend.alm.feign.name:alm-transcend-datadriven}", url = "${transcend.alm.feign.url:}")
public interface SystemRequirementFeignClient {


    /**
     * 简单列表查询
     *
     * @param searchKey 关键词搜索，通过编码coding进行模糊搜索
     * @return 简单SR结构列表，不分页且最多返回200条
     */
    @GetMapping(value = "api/alm/sr/simpleList")
    BaseResponse<List<SimpleSrDTO>> simpleList(@RequestParam(required = false) String searchKey);


    /**
     * SR列表查询接口
     *
     * @param ao 查询参数
     * @return 简单SR结构列表，最大返回2000条数据
     */
    @PostMapping(value = "api/alm/sr/querySimpleList")
    BaseResponse<List<SimpleSrDTO>> querySimpleList(@RequestBody QuerySimpleListAO ao);
}
