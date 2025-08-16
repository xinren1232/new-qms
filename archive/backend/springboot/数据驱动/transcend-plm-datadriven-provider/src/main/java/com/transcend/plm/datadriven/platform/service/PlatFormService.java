package com.transcend.plm.datadriven.platform.service;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.platform.feign.client.UacUserFeignService;
import com.transcend.plm.datadriven.platform.feign.model.qo.UserQueryRequest;
import com.transcend.plm.datadriven.platform.feign.model.dto.PlatFormUserDTO;
import com.transsion.framework.sdk.open.dto.OpenResponse;
import com.transsion.framework.uac.model.dto.PageDTO;
import com.transsion.framework.uac.model.request.UacRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
@Slf4j
public class PlatFormService implements IPlatformService {

    @Resource
    private UacUserFeignService uacUserFeignService;

    /**
     * @param keywords
     * @return {@link PageDTO }<{@link PlatFormUserDTO }>
     */
    @Override
    public PageDTO<PlatFormUserDTO> queryPlatformUser(String keywords) {
        UacRequest<UserQueryRequest> userQueryRequestUacRequest = new UacRequest<>();
        UserQueryRequest userQueryRequest = new UserQueryRequest().setKeyword(keywords);
        userQueryRequestUacRequest.setParam(userQueryRequest);
        userQueryRequestUacRequest.setCurrent(1);
        userQueryRequestUacRequest.setSize(99);
        log.info("查询平台用户信息，请求参数：{}", JSON.toJSONString(userQueryRequestUacRequest));
        OpenResponse<PageDTO<PlatFormUserDTO>> pageDTOOpenResponse = uacUserFeignService.queryWebPages(userQueryRequestUacRequest);
        log.info("查询平台用户信息，响应参数：{}", JSON.toJSONString(pageDTOOpenResponse));
        if(Objects.isNull(pageDTOOpenResponse)  || !pageDTOOpenResponse.isSuccess()){
            log.error("查询平台用户信息失败，原因如下：{}",  pageDTOOpenResponse.getMessage());
            throw new PlmBizException("查询平台用户信息失败，请联系IT管理员");
        }
        return pageDTOOpenResponse.getData();
    }
}
