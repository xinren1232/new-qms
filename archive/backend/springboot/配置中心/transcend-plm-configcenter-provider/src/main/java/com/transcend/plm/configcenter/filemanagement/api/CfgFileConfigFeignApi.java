package com.transcend.plm.configcenter.filemanagement.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgFileConfigFeignClient;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileCopyRuleVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileLibraryVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeRelRuleNameVo;
import com.transcend.plm.configcenter.api.model.filemanagement.vo.CfgFileTypeVo;
import com.transcend.plm.configcenter.filemanagement.service.ICfgFileConfigAppService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "CfgFileConfig Controller", tags = "API-文件配置-控制器")
@RestController
public class CfgFileConfigFeignApi implements CfgFileConfigFeignClient {
    @Resource
    private ICfgFileConfigAppService cfgFileConfigAppService;

    public TranscendApiResponse<CfgFileTypeVo> getCfgFileTypeByCode(String code){
        return TranscendApiResponse.success(cfgFileConfigAppService.getCfgFileTypeByCode(code));
    }

    @Override
    public TranscendApiResponse<CfgFileLibraryVo> getDefaultCfgFileLibrary() {
        return TranscendApiResponse.success(cfgFileConfigAppService.getDefaultCfgFileLibrary());
    }

    @Override
    public TranscendApiResponse<CfgFileLibraryVo> getCfgFileLibraryByAddress(String address) {
        return TranscendApiResponse.success(cfgFileConfigAppService.getCfgFileLibraryByAddress(address));
    }

    @Override
    public TranscendApiResponse<List<CfgFileLibraryVo>> getCfgFileLibraryByCodes(List<String> codes) {
        return TranscendApiResponse.success(cfgFileConfigAppService.getCfgFileLibraryByCodes(codes));
    }

    @Override
    public TranscendApiResponse<List<CfgFileLibraryVo>> listCfgFileLibrary() {
        return TranscendApiResponse.success(cfgFileConfigAppService.listCfgLiabrary());
    }

    @Override
    public TranscendApiResponse<List<CfgFileCopyRuleVo>> getCfgFileCopyRuleByFileTypeCode(String code) {
        return TranscendApiResponse.success(cfgFileConfigAppService.getCfgFileCopyRuleByFileTypeCode(code));
    }

    @Override
    public TranscendApiResponse<List<CfgFileTypeRelRuleNameVo>> getCfgRuleListByName(String name) {
        return TranscendApiResponse.success(cfgFileConfigAppService.getCfgRuleListByName(name));
    }
}
