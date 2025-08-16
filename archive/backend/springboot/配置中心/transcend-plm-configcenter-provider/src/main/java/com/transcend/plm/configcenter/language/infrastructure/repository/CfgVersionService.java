package com.transcend.plm.configcenter.language.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgVersionPo;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgVersionService extends IService<CfgVersionPo> {
    /**
     * 版本增加
     * @param name
     * @return
     */
    Long increment(String name);

    /**
     * 获取当前版本
     * @param name
     * @return
     */
    Long getVersionByName(String name);
}
