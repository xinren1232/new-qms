package com.transcend.plm.datadriven.api.feign;

import com.transcend.plm.datadriven.api.feign.fallback.DictFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author unknown
 */
@FeignClient(
        name = "${transsion.dictionary.service-name:service-base-dictionary}",
        fallbackFactory = DictFeignFallback.class
)
public interface DictionaryFeign {
    /**
     * getChangeMessage
     *
     * @param appCode appCode
     * @param codes   codes
     * @return {@link Object}
     */
    @GetMapping({"/base/dictionary/getValuesByCodes"})
    Object getChangeMessage(@RequestParam("appCode") String appCode, @RequestParam("codes") List<String> codes);
}
