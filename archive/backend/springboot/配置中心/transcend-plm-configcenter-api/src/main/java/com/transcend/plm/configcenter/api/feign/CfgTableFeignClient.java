package com.transcend.plm.configcenter.api.feign;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 16:07
 * @since 1.0
 */

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transsion.framework.annotation.PermissionLimit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 * 本地调试加：url = "http://127.0.0.1:8081/",
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface CfgTableFeignClient {
    /**
     * 获取表包括属性信息
     * @param bid 表bid
     * @return
     */
    @GetMapping("/api/cfg/table/getByBid/{bid}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgTableVo> getByBid(@PathVariable String bid);
    /**
     * 获取表包括属性信息
     * @param bid 表bid
     * @return
     */
    @GetMapping("/api/cfg/table/getTableAndAttributeByBid/{bid}")
    @PermissionLimit(limit = false)
    TranscendApiResponse<CfgTableVo> getTableAndAttributeByBid(@PathVariable String bid);

    @GetMapping("/api/cfg/table/getTableAndAttributeByModelCode/{modelCode}")
    @PermissionLimit(limit = false)
    @Cacheable(value = "TABLE_AND_ATTR", key = "#modelCode")
    TranscendApiResponse<CfgTableVo> getTableAndAttributeByModelCode(@PathVariable String modelCode);


}
