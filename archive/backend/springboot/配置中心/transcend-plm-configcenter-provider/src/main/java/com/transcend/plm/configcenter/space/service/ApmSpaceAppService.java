package com.transcend.plm.configcenter.space.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.space.repository.po.ApmSpaceApp;

import java.util.List;

/**
 *
 */
public interface ApmSpaceAppService extends IService<ApmSpaceApp> {

    /**
     * 根据对象Code获取 使用该对象code的所有应用
     *
     * @param modelCode 对象CODE
     * @return {@link List< ApmSpaceApp>}
     * @date 2024/5/11 15:26
     * @author quan.cheng
     */
    List<ApmSpaceApp> getByMc(String modelCode);

    List<ApmSpaceApp> getByMcs(List<String> modelCodes);
}
