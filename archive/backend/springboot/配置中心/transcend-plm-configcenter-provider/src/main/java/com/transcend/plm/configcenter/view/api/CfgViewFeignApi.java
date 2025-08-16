package com.transcend.plm.configcenter.view.api;

import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.CfgViewFeignClient;
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.object.domain.service.CfgObjectViewRuleDomainService;
import com.transcend.plm.configcenter.view.application.service.ICfgViewApplicationService;
import com.transcend.plm.configcenter.view.domain.service.CfgViewDomainService;
import com.transcend.plm.configcenter.view.infrastructure.repository.CfgViewService;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import io.swagger.annotations.Api;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API-视图-控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 14:24
 * @since 1.0
 */
@Api(value = "CfgView Controller", tags = "API-视图-控制器")
@Validated
@RestController
public class CfgViewFeignApi implements CfgViewFeignClient {

    @Resource
    private CfgObjectViewRuleDomainService cfgObjectViewRuleDomainService;
    @Resource
    private ICfgViewApplicationService iCfgViewApplicationService;
    @Resource
    private CfgViewDomainService cfgViewDomainService;


    @Override
    public TranscendApiResponse<CfgViewVo> getOptimalMatchView(CfgViewRuleMatchDto ruleMatchDto) {
        // 1.存在视图bid,则直接匹配
        String viewBid = ruleMatchDto.getViewBid();
        if (StringUtil.isNotBlank(viewBid)) {
            // 直接返回
            return TranscendApiResponse.success(
                    cfgViewDomainService.getByBid(viewBid)
            );
        }
        String modelCode = ruleMatchDto.getModelCode();
        // 2.获取对象匹配规则
        viewBid = cfgObjectViewRuleDomainService
                .getViewBidByOptimalMatch(ruleMatchDto);
        // 3.如果都不匹配则获取视图的第一个
        if (StringUtil.isBlank(viewBid)) {
            return TranscendApiResponse.success(
                    cfgViewDomainService.getByModelCode(modelCode)
            );
        }
        // 直接返回
        return TranscendApiResponse.success(
                cfgViewDomainService.getByBid(viewBid)
        );
    }

    @Override
    public TranscendApiResponse<CfgViewVo> getLogView(CfgViewRuleMatchDto ruleMatchDto) {
        String modelCode = ruleMatchDto.getModelCode();
        String tag = "TABLE";
        String viewBid = cfgObjectViewRuleDomainService
                .getViewBidByModelCodeAndTag(modelCode, tag);
        return TranscendApiResponse.success(
                cfgViewDomainService.getByBid(viewBid)
        );
    }

    @Override
    public TranscendApiResponse<CfgViewVo> saveOrUpdate(@PathVariable("bid") String bid,
                                                        @RequestBody CfgViewDto cfgViewDto) {
        return TranscendApiResponse.success(iCfgViewApplicationService.saveOrUpdate(bid, cfgViewDto));
    }

    @Override
    public TranscendApiResponse<CfgViewVo> saveOrUpdateView(CfgViewDto cfgViewDto) {
        return TranscendApiResponse.success(iCfgViewApplicationService.saveOrUpdate(cfgViewDto));
    }

    @Override
    public TranscendApiResponse<Boolean> copyViews(CfgViewDto cfgViewDto) {
        return TranscendApiResponse.success(iCfgViewApplicationService.copyViews(cfgViewDto));
    }

    @Override
    public TranscendApiResponse<CfgViewVo> getView(ViewQueryParam param) {
        return TranscendApiResponse.success(iCfgViewApplicationService.getView(param));
    }

    @Override
    public TranscendApiResponse<Map<String, CfgViewVo>> getViews(ViewQueryParam param) {
        return TranscendApiResponse.success(iCfgViewApplicationService.getViews(param));
    }

    @Override
    public TranscendApiResponse<List<CfgViewVo>> listView(String belongBid) {
        return TranscendApiResponse.success(iCfgViewApplicationService.listView(belongBid));
    }

    @Override
    public TranscendApiResponse<List<CfgViewVo>> listView(List<String> belongBids) {
        return TranscendApiResponse.success(iCfgViewApplicationService.listView(belongBids));
    }

    @Override
    public TranscendApiResponse<CfgViewVo> getByBid(@PathVariable("bid") String bid) {
        return TranscendApiResponse.success(cfgViewDomainService.getByBid(bid));
    }

    @Override
    public TranscendApiResponse<Map<String, CfgViewVo>> getByBids(@RequestBody Set<String> bids) {
        return TranscendApiResponse.success(cfgViewDomainService.getByBids(bids));
    }

    @Override
    @Cacheable(value = CacheNameConstant.OBJECT_VIEW, key = "#bid")
    public TranscendApiResponse<List<CfgViewMetaDto>> listMetaModels(@PathVariable("bid") String bid) {
        return TranscendApiResponse.success(cfgViewDomainService.getMetaModelsByBid(bid));
    }

    @Override
    public TranscendApiResponse<List<CfgViewMetaDto>> listMetaModels(ViewQueryParam param) {
        return TranscendApiResponse.success(iCfgViewApplicationService.getMetaModelsByParam(param));
    }

    @Override
    public TranscendApiResponse<List<CfgViewVo>> listTypeOrDefaultView(ViewQueryParam param) {
        return TranscendApiResponse.success(iCfgViewApplicationService.listTypeOrDefaultView(param));
    }

}
