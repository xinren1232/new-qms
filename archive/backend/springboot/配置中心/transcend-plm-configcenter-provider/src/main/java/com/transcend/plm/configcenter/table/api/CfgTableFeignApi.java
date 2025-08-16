package com.transcend.plm.configcenter.table.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgTableFeignClient;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 表api
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 14:24
 * @since 1.0
 */
@Slf4j
@Api(value = "CfgObject Controller", tags = "API-table-控制器")
@Validated
@RestController
public class CfgTableFeignApi implements CfgTableFeignClient {

    @Resource
    private CfgTableDomainService cfgTableDomainService;


    @Override
    public TranscendApiResponse<CfgTableVo> getByBid(String bid) {
        return TranscendApiResponse.success(
                cfgTableDomainService.getByBid(bid)
        );
    }

    @Override
    public TranscendApiResponse<CfgTableVo> getTableAndAttributeByBid(String bid) {
        return TranscendApiResponse.success(
                cfgTableDomainService.getTableAndAttributeByBid(bid)
        );
    }

    @Override
    public TranscendApiResponse<CfgTableVo> getTableAndAttributeByModelCode(String modelCode) {
        return TranscendApiResponse.success(
                cfgTableDomainService.getTableAndAttributeByModelCode(modelCode)
        );
    }
}
