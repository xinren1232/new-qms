package com.transcend.plm.datadriven.apm.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.dto.SendFeishuRequest;
import com.transcend.plm.datadriven.apm.integration.consumer.NotifyConsumer;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.powerjob.notify.ExecuteTriggerNotify;
import com.transcend.plm.datadriven.apm.powerjob.notify.NotifyExecuteRecordConfig;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author unknown
 */
@RestController
@Slf4j
@Api(tags = {"推送中心Controller"})
public class ApmPushCenterController {

    @ApiOperation("发送飞书提醒")
    @PostMapping(value = "/api/app/apm/pushCenter/sendFeishu")
    public TranscendApiResponse<Boolean> sendFeishu(@RequestBody SendFeishuRequest request){
        try {
            /**发飞书*/
            PushCenterFeishuBuilder builder = PushCenterFeishuBuilder.builder()
                    .title(request.getTitle());
            if (StringUtils.isNotEmpty(request.getContent())) {
                builder.content(request.getContent());
            }
            if (CollectionUtils.isNotEmpty(request.getContents())){
                builder.content(request.getContents());
            }
            builder.receivers(request.getReceivers())
                    .send();
        } catch (Exception e) {
            log.error("飞书发送失败", e);
        }
        return TranscendApiResponse.success(true);
    }

}
