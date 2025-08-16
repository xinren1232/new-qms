package com.transcend.plm.datadriven.apm.event.entity.base;

import com.alibaba.excel.util.StringUtils;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transsion.framework.tool.SpringBeanHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bin.yin
 * @description: 关系新增 事件入参
 * @version:
 * @date 2024/06/13 17:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationEventHandlerParam extends EventHandlerParam {
    private CfgObjectVo relationCfgObjectVo;
    public <T extends EventHandlerParam> T initRelationMeta(String relationModelCode) {
        if (StringUtils.isBlank(relationModelCode)) {
            return (T) this;
        }
        CfgObjectFeignClient cfgObjectFeignClient = SpringBeanHelper.getBean(CfgObjectFeignClient.class);
        relationCfgObjectVo = cfgObjectFeignClient.getByModelCode(relationModelCode).getCheckExceptionData();
        return (T) this;
    }
}
