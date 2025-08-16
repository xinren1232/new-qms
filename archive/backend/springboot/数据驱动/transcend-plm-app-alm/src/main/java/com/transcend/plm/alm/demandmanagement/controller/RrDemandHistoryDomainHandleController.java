package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.service.RrDemandHistoryDomainHandleService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yuanhu.huang
 * @version v1.0.0
 * @description 需求管理定制业务控制器
 * @date 2024/09/7 11:03
 **/
@Api(value = "RrDemandHistoryDomainHandleController", tags = "RR需求管理-控制器")
@RestController
@RequestMapping(value = ("/alm/rrDemandManager"))
public class RrDemandHistoryDomainHandleController {

    @Resource
    private RrDemandHistoryDomainHandleService rrDemandHistoryDomainHandleService;

    @PostMapping("/handleRrDomain")
    public TranscendApiResponse<Boolean> handleRrDomain() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleDomain());
    }

    @PostMapping("/handleIrDomain")
    public TranscendApiResponse<Boolean> handleIrDomain() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleIrDomain());
    }

    @PostMapping("/handleRrModule")
    public TranscendApiResponse<Boolean> handleRrModule() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleRrModule());
    }

    @PostMapping("/handleIrModule")
    public TranscendApiResponse<Boolean> handleIrModule() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleIrModule());
    }

    @PostMapping("/handleSrModule")
    public TranscendApiResponse<Boolean> handleSrModule() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleSrModule());
    }

    @PostMapping("/handleArModule")
    public TranscendApiResponse<Boolean> handleArModule() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleArModule());
    }

    @PostMapping("/handleArDomain")
    public TranscendApiResponse<Boolean> handleArDomain() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleArDomain());
    }

    @PostMapping("/handleSrDomain")
    public TranscendApiResponse<Boolean> handleSrDomain() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleSrDomain());
    }

    @PostMapping("/handleIrOsVersion")
    public TranscendApiResponse<Boolean> handleIrOsVersion() {
        return TranscendApiResponse.success(rrDemandHistoryDomainHandleService.handleIrOsVersion());
    }
}
