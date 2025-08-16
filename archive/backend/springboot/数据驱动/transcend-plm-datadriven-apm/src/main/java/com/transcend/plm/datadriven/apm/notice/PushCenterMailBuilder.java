package com.transcend.plm.datadriven.apm.notice;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transsion.sdk.core.DefaultTransClient;
import com.transsion.sdk.core.exceptions.ClientException;
import com.transsion.sdk.push.domain.Attachment;
import com.transsion.sdk.push.domain.Receiver;
import com.transsion.sdk.push.enums.IdType;
import com.transsion.sdk.push.model.SendMailRequest;
import com.transsion.sdk.push.model.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 发送邮件请求类
 * @Author peng.qin
 * @Version 1.0
 * @Date 2022-08-24 11:55
 **/
@Slf4j
public class PushCenterMailBuilder {
    private static DefaultTransClient client;
    private SendMailRequest sendMailRequest;
    private PushCenterMailBuilder(){}
    public static PushCenterMailBuilder builder() {
        PushCenterMailBuilder messageBuilder = new PushCenterMailBuilder();
        initClientAndRequest(messageBuilder);
        return messageBuilder;
    }

    private static void initClientAndRequest(PushCenterMailBuilder messageBuilder) {
        PushCenterProperties pushCenterProperties = PlmContextHolder.getBean(PushCenterProperties.class);
        if (Objects.isNull(client)) {
            synchronized (PushCenterMailBuilder.class) {
                if (Objects.isNull(client)) {
                    client = new DefaultTransClient(pushCenterProperties.getGateway(), pushCenterProperties.getAppKey(), pushCenterProperties.getAppSecret());
                }
            }
        }
        SendMailRequest request = new SendMailRequest();
        request.setTenantKey(pushCenterProperties.getAppKey());
        request.setTemplateCode(pushCenterProperties.getPdcConclusionTemplateCode());
        messageBuilder.setSendMailRequest(request);
    }

    private SendMailRequest getSendMailRequest() {
        return sendMailRequest;
    }

    private void setSendMailRequest(SendMailRequest sendMailRequest) {
        this.sendMailRequest = sendMailRequest;
    }

    /**
     * 需要模板中主题配置为${subject}
     * @param subject 邮件主题
     * @return
     */
    public PushCenterMailBuilder subject(String subject) {
        Map<String, String> variables = putVariables("subject",subject);
        return this;
    }

    /**
     * 需要模板中主题配置为${content}
     * @param content 邮件内容
     * @return
     */
    public PushCenterMailBuilder content(String content) {
        Map<String, String> variables = putVariables("content",content);
        return this;
    }

    /**
     * 添加主送人
     * @param mainReceivers
     * @return
     */
    public PushCenterMailBuilder mailMainReceivers(List<String> mainReceivers) {
        if (CollectionUtils.isEmpty(mainReceivers)) {
            return this;
        }
        Receiver receiver = Receiver.builder().idType(IdType.EMP_NO.getType()).ids(mainReceivers).build();
        List<Receiver> receivers = addReceiver(receiver);
        return this;
    }

    /**
     * 添加抄送人
     * @param css
     * @return
     */
    public PushCenterMailBuilder mailCopyReceivers(List<String> css) {
        if (CollectionUtils.isEmpty(css)) {
            return this;
        }
        List<Receiver> ccs = CollectionUtils.isEmpty(sendMailRequest.getCcs()) ? Lists.newArrayList() : sendMailRequest.getCcs();
        Receiver receiver = Receiver.builder().idType(IdType.EMP_NO.getType()).ids(css).build();
        ccs.add(receiver);
        sendMailRequest.setCcs(ccs);
        return this;
    }

    /**
     * 慎用！！
     * 需要用pmtoffice邮箱发送邮件时，才调用。
     * @param isPMTOffice
     * @return
     */
    public PushCenterMailBuilder isPMTOffice(boolean isPMTOffice) {
        sendMailRequest.setIsCustomSender(isPMTOffice);
        return this;
    }

    public PushCenterMailBuilder templateCode(String templateCode) {
        sendMailRequest.setTemplateCode(templateCode);
        return this;
    }

    public PushCenterMailBuilder attachments(List<Attachment> attachments){
        if (CollectionUtils.isEmpty(attachments)){
            return this;
        }
        sendMailRequest.setAttachments(attachments);
        return this;
    }


    public PushSendResult send() {
        try {
            log.info("begin send message : {}", JSON.toJSONString(sendMailRequest));
            SendResponse response = client.execute(sendMailRequest);
            if (response.isSuccess()) {
                log.info("==>==>发送成功----\n{}",JSON.toJSONString(response));
                return PushSendResult.builder().success(true).message(JSON.toJSONString(response)).build();
            } else {
                log.error("==>==>发送失败----\n{}",JSON.toJSONString(response));
                return PushSendResult.builder().success(false).message(JSON.toJSONString(response)).build();
            }
        } catch (ClientException e) {
            throw new RuntimeException("send message fail",e);
        }
    }

    private List<Receiver> addReceiver(Receiver receiver) {
        List<Receiver> receivers = CollectionUtils.isEmpty(sendMailRequest.getReceivers()) ? Lists.newArrayList() : sendMailRequest.getReceivers();
        receivers.add(receiver);
        sendMailRequest.setReceivers(receivers);
        return receivers;
    }

    private Map<String, String> putVariables(String key,String value) {
        Map<String, String> variables = MapUtils.isEmpty(sendMailRequest.getVariables()) ? Maps.newHashMap() : sendMailRequest.getVariables();
        variables.put(key, value);
        sendMailRequest.setVariables(variables);
        return variables;
    }
}
