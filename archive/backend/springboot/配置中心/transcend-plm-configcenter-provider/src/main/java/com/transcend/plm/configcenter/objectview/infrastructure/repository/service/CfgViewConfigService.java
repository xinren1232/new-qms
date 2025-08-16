package com.transcend.plm.configcenter.objectview.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo.CfgViewConfigQo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigVo;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfig;
import com.transsion.framework.dto.BaseRequest;

/**
 *
 */
public interface CfgViewConfigService extends IService<CfgViewConfig> {
    PagedResult<CfgViewConfigVo> page(BaseRequest<CfgViewConfigQo> pageQo);
}
