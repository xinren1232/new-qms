package com.transcend.plm.datadriven.apm.feign.service;

import com.transcend.plm.datadriven.apm.feign.model.qo.UserQueryRequest;
import com.transcend.plm.datadriven.apm.feign.model.vo.PlatFormUserDTO;
import com.transsion.framework.sdk.open.config.OpenAuthorizeConfig;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.factory.UacUserFallbackFactory;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.request.UacRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/14 18:06
 * @Describe
 */
@FeignClient(name = "uac-user-service",
        fallbackFactory = UacUserFallbackFactory.class, configuration = OpenAuthorizeConfig.class)
public interface UacUserFeignService {
    /**
     * queryWebPages
     *
     * @param request request
     * @return {@link OpenResponse<PageDTO<PlatFormUserDTO>>}
     */
    @PostMapping("v2/api/uac-user/user/webPages")
    OpenResponse<PageDTO<PlatFormUserDTO>> queryWebPages(@RequestBody UacRequest<UserQueryRequest> request);
}
