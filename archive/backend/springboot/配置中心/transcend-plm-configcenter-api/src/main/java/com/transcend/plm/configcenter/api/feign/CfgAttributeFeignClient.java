package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/05/08 10:16
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgAttributeFeignClient {

    @GetMapping("/api/manager/cfg/attribute/standard/getByCode/{code}")
    TranscendApiResponse<CfgAttributeVo> getByCode(@PathVariable("code") String code);
}
