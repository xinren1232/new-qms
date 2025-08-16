package com.transcend.plm.configcenter.objectview.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfigAttr;

import java.util.List;

/**
 *
 */
public interface CfgViewConfigAttrService extends IService<CfgViewConfigAttr> {

    int saveConfigAttrs(List<CfgViewConfigAttr> viewConfigAttrList);

}
