package com.transcend.plm.configcenter.attribute.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgAttributeFeignClient;
import com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.attribute.domain.service.CfgAttributeDomainService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/05/08 10:18
 */
@Api(value = "CfgAttributeFeignApi", tags = "API-属性管理-控制器")
@RestController

public class CfgAttributeFeignApi implements CfgAttributeFeignClient {
    @Resource
    private CfgAttributeDomainService cfgAttributeDomainService;
    @Override
    public TranscendApiResponse<CfgAttributeVo> getByCode(String code) {
        return TranscendApiResponse.success(cfgAttributeDomainService.getByCode(code));
    }
}
