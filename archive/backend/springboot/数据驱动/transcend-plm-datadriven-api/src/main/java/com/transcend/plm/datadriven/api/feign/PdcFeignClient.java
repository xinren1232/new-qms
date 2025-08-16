package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.model.dto.RelationCodeDTO;
import com.transcend.plm.datadriven.api.model.vo.PlmDictViewVO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author weixin.wang
 */
@FeignClient(name = "${transsion.pdc.feign.name:service-pdc}", url = "${transsion.pdc.feign.url:}")
public interface PdcFeignClient {

    /**
     * plmQueryDictByCode
     *
     * @param code code
     * @return {@link ResponseVO<List<PlmDictViewVO>>}
     */
    @ApiOperation("根据code 查询字典")
    @GetMapping("/pdc/dict/plmQueryDictByCodes")
    ResponseVO<List<PlmDictViewVO>> plmQueryDictByCode(@RequestParam("code") String code);

    /**
     * 根据CODE查询code关联的关系数据
     *
     * @param dictRelationPO dictRelationPO
     * @return {@link ResponseVO<Map<String,Object>>}
     */
    @ApiOperation("根据CODE查询code关联的关系数据")
    @PostMapping("/pdc/dictRelation/getDictRelationByCode")
    ResponseVO<Map<String, Object>> getDictRelationByCode(@RequestBody RelationCodeDTO dictRelationPO);
}
