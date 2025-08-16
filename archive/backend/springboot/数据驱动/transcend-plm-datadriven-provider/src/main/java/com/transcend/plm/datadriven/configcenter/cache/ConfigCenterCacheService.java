package com.transcend.plm.datadriven.configcenter.cache;

import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgTableFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.configcenter.model.enums.ConfigKeyEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * 配置中心缓存
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/6/1 14:37
 * @since 1.0
 */
@Component
public class ConfigCenterCacheService {

    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;
    @Resource
    private CfgViewFeignClient cfgViewFeignClient;
    @Resource
    private CfgTableFeignClient cfgTableFeignClient;


    /**
     * 获取最优匹配视图
     * @param dto {@link CfgViewRuleMatchDto }
     * @return {@link CfgViewVo }
     */
    public CfgViewVo getOptimalMatchView(CfgViewRuleMatchDto dto) {
        return cfgViewFeignClient.getOptimalMatchView(dto).getCheckExceptionData();
    }

    /**
     * 批量获取 前端需要的表头级别配置（视图/生命周期/仅仅与生命周期无关系权限）
     *
     * @param modelCode 模型编码
     * @return Map<String, Object>
     */
    public Map<String, Object> listDefaultConfigByModelCode(String modelCode) {
        Set<String> configKeys = Sets.newHashSet();
        ConfigKeyEnum[] values = ConfigKeyEnum.values();
        Arrays.stream(values).forEach(v -> {
                    String code = v.getCode();

                }

        );
        return null;
    }
}
