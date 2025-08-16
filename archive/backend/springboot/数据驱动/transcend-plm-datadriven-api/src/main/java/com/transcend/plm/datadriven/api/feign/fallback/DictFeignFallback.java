package com.transcend.plm.datadriven.api.feign.fallback;

import com.transcend.plm.datadriven.api.feign.DictionaryFeign;
import com.transcend.plm.datadriven.api.feign.ObjectModelFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public class DictFeignFallback implements FallbackFactory<DictionaryFeign> {
    @Override
    public DictionaryFeign create(Throwable cause) {
        return null;
    }
}