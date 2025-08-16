package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.apm.space.model.ApmPersonMemoryParam;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmPersonMemory;

import java.util.List;
import java.util.Map;

/**
 * 个人记忆功能
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/10/7 18:29
 * @since 1.0
 */
public interface IApmPersonalMemoryService {
    /**
     * saveOrUpdate
     *
     * @param apmPersonMemory apmPersonMemory
     * @return {@link Boolean}
     */
    Boolean saveOrUpdate(ApmPersonMemory apmPersonMemory);

    /**
     * get
     *
     * @param apmPersonMemoryParam apmPersonMemoryParam
     * @return {@link Map<String, Object>}
     */
    Map<String, Object> get(ApmPersonMemoryParam apmPersonMemoryParam);

    /**
     * updatePartialContent
     *
     * @param apmPersonMemoryParam apmPersonMemoryParam
     * @return {@link Boolean}
     */
    Boolean updatePartialContent(ApmPersonMemoryParam apmPersonMemoryParam);

    /**
     * getPartialContent
     *
     * @param category category
     * @param code     code
     * @param keys     keys
     * @return {@link Map<String, Object>}
     */
    Map<String, Object> getPartialContent(String category, String code, List<String> keys);
}
