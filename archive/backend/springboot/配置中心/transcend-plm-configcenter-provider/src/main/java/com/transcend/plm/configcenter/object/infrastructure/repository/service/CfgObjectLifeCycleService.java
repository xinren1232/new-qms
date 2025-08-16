package com.transcend.plm.configcenter.object.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCycle;


/**
 *
 */
public interface CfgObjectLifeCycleService extends IService<CfgObjectLifeCycle> {

   CfgObjectLifeCycle getByModelCode(String modelCode);
}
