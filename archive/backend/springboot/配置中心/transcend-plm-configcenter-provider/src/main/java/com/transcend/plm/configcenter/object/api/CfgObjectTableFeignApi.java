package com.transcend.plm.configcenter.object.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgObjectTableFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transcend.plm.configcenter.object.application.service.ICfgRelationObjectConfigAppService;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 14:24
 * @since 1.0
 */
@Api(value = "CfgObjectTable Controller", tags = "API-对象-控制器")
@Validated
@RestController
public class CfgObjectTableFeignApi implements CfgObjectTableFeignClient {


    @Override
    public TranscendApiResponse<CfgObjectVo> getByModelCode(String modelCode) {
        return null;
    }
}
