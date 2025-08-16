package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileCopyRuleVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileLibraryVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yuanhu.huang
 * @version:
 * @date 2024/04/16 09:24
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgFileConfigFeignClient {

    @GetMapping("/api/cfg/filemanagement/getCfgFileCode/{code}")
    TranscendApiResponse<CfgFileTypeVo> getCfgFileTypeByCode(@PathVariable("code") String code);

    @GetMapping("/api/cfg/filemanagement/getDefaultCfgFileLibrary")
    TranscendApiResponse<CfgFileLibraryVo> getDefaultCfgFileLibrary();

    @GetMapping("/api/cfg/filemanagement/getCfgFileLibraryByAddress/{address}")
    TranscendApiResponse<CfgFileLibraryVo> getCfgFileLibraryByAddress(@PathVariable("address") String address);

    @PostMapping("/api/cfg/filemanagement/getCfgFileLibraryByCodes")
    TranscendApiResponse<List<CfgFileLibraryVo>> getCfgFileLibraryByCodes(@RequestBody List<String> codes);

    @GetMapping("/api/cfg/filemanagement/getCfgFileCopyRuleByFileTypeCode/{code}")
    TranscendApiResponse<List<CfgFileCopyRuleVo>> getCfgFileCopyRuleByFileTypeCode(@PathVariable("code") String code);

    @GetMapping("/api/cfg/filemangement/getCfgRuleListByName/{name}")
    TranscendApiResponse<List<CfgFileTypeRelRuleNameVo>> getCfgRuleListByName(@PathVariable("name") String name);

    @GetMapping("/api/cfg/filemanagement/cfgFileLibrary/list")
    TranscendApiResponse<List<CfgFileLibraryVo>> listCfgFileLibrary();
}
