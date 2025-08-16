package com.transcend.plm.datadriven.apm.event.entity.base;

import com.alibaba.excel.util.StringUtils;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transsion.framework.tool.SpringBeanHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * @author bin.yin
 * @description: 事件处理基础参数
 * @version:
 * @date 2024/06/13 14:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class  EventHandlerParam {
    /** 空间bid **/
    private String spaceBid;
    /** 应用基础信息 **/
    private ApmSpaceApp apmSpaceApp;
    /** 应用对象基础信息 **/
    private CfgObjectVo cfgObjectVo;

    /**
     * 通过应用bid 获取应用 + 对象基础信息
     * @param spaceAppBid 应用bid
     * @return ApmSpaceAppAndObjectVo
     */
    public <T extends EventHandlerParam> T initAppAndObj(String spaceAppBid) {
        if (StringUtils.isBlank(spaceAppBid)) {
            return (T) this;
        }
        ApmSpaceAppService apmSpaceAppService = SpringBeanHelper.getBean(ApmSpaceAppService.class);
        apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        Assert.notNull(apmSpaceApp, "应用信息不存在");
        CfgObjectFeignClient cfgObjectFeignClient = SpringBeanHelper.getBean(CfgObjectFeignClient.class);
        cfgObjectVo = cfgObjectFeignClient.getByModelCode(apmSpaceApp.getModelCode()).getCheckExceptionData();
        this.setSpaceBid(apmSpaceApp.getSpaceBid());
        return (T) this;
    }

    /**
     * 获取对象bid
     * @return 对象bid
     */
    public String getObjBid() {
        return cfgObjectVo == null?"":cfgObjectVo.getBid();
    }

}
