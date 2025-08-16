package com.transcend.plm.datadriven.platform.service;


import com.transcend.plm.datadriven.platform.feign.model.dto.PlatFormUserDTO;
import com.transsion.framework.uac.model.dto.PageDTO;


/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2023/11/16
 */
public interface IPlatformService {

    /**
     * 中转前端查询用户中心的用户信息（同前端查询用户中心入参）
     * @param keywords
     * @return
     */
    PageDTO<PlatFormUserDTO> queryPlatformUser(String keywords);
}
