package com.transcend.plm.configcenter.dictionary.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.dictionary.application.service.ICfgDictionaryApplicationService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/08 15:43
 */
@Api(value = "Dictionary Controller", tags = "API-字典管理-控制器")
@RestController
public class DictionaryFeignApi implements DictionaryFeignClient {

    @Resource
    private ICfgDictionaryApplicationService dictionaryApplicationService;

    @Override
    public TranscendApiResponse<List<CfgDictionaryVo>> listDictionaryAndItemByCodesAndEnableFlags(CfgDictionaryComplexQo qo) {
        return TranscendApiResponse.success(dictionaryApplicationService.listDictionaryAndItemByCodesAndEnableFlags(qo.getCodes(), qo.getEnableFlags()));
    }
}
