package com.transcend.plm.datadriven.apm.notice;

import lombok.Builder;
import lombok.Data;

/**
 * @author quan.cheng
 * @description 存储发送信息的结果
 * @title PushSendResult
 * @date 2024/1/29 11:10
 */
@Data
@Builder
public class PushSendResult {
    private boolean success;
    private String message;
}
