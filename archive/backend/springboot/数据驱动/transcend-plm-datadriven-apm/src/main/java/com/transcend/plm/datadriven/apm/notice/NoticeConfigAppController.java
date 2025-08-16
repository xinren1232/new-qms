package com.transcend.plm.datadriven.apm.notice;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.service.NotifyAppService;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@RestController
@Api(value = "Apm NoticeConfigAppController", tags = "空间-应用配置驱动-控制器")
@RequestMapping(value ="/apm/notice/")
public class NoticeConfigAppController {
    @Resource
    private NotifyAppService notifyAppService;

    @ApiOperation("保存通知配置信息")
    @PostMapping("/saveConfig")
    public TranscendApiResponse<Boolean> saveConfig(@RequestBody NotifyConfigDto dto){
        return TranscendApiResponse.success(notifyAppService.saveNotifyConfig(dto));
    }

    @ApiOperation("保存通知配置信息")
    @PostMapping("/saveConfigs")
    public TranscendApiResponse<Boolean> saveConfigs(@RequestBody List<NotifyConfigDto> dtos){
        return TranscendApiResponse.success(notifyAppService.saveNotifyConfigs(dtos));
    }

    @ApiOperation("查询通知配置信息")
    @PostMapping("/listConfigs")
    public TranscendApiResponse<List<NotifyConfigVo>> listConfigs(@RequestBody NotifyConfigDto dto){
        return TranscendApiResponse.success(notifyAppService.listNotifyConfig(dto));
    }

}
