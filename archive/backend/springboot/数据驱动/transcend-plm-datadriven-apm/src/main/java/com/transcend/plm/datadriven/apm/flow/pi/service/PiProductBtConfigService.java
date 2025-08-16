package com.transcend.plm.datadriven.apm.flow.pi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.pi.domain.PiProductBtConfig;

/**
 * @author unknown
 */
public interface PiProductBtConfigService extends IService<PiProductBtConfig> {
    /**
     * getProductManager
     *
     * @param btDepartmentCode btDepartmentCode
     * @return {@link String}
     */
    String getProductManager(String btDepartmentCode);
}
