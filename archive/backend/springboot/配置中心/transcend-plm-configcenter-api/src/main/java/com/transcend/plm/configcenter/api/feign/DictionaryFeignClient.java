package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/08 15:44
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface DictionaryFeignClient {

    @ApiOperation("根据字典codes+enableFlag查询字典基本信息+条目")
    @PostMapping("/api/manager/cfg/dictionary/listDictionaryAndItemByCodesAndEnableFlags")
    TranscendApiResponse<List<CfgDictionaryVo>> listDictionaryAndItemByCodesAndEnableFlags(@RequestBody CfgDictionaryComplexQo qo);
}
