package com.transcend.plm.datadriven.apm.notice;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transsion.sdk.core.DefaultTransClient;
import com.transsion.sdk.core.exceptions.ClientException;
import com.transsion.sdk.push.domain.Receiver;
import com.transsion.sdk.push.domain.message.CardLink;
import com.transsion.sdk.push.domain.message.body.CardBody;
import com.transsion.sdk.push.domain.message.element.Module;
import com.transsion.sdk.push.domain.message.element.*;
import com.transsion.sdk.push.enums.*;
import com.transsion.sdk.push.model.SendIMRequest;
import com.transsion.sdk.push.model.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Program transsion-ipm
 * @Description 直接调用平台提供SDK进行消息发送,发送飞书卡片消息
 * @Author peng.qin
 * @Version 1.0
 * @Date 2022-08-24 11:55
 **/
@Slf4j
public class PushCenterFeishuBuilder {
    private static DefaultTransClient client;
    private SendIMRequest sendIMRequest;
    private PushCenterFeishuBuilder(){}
    public static PushCenterFeishuBuilder builder() {
        PushCenterFeishuBuilder messageBuilder = new PushCenterFeishuBuilder();
        initClientAndRequest(messageBuilder);
        return messageBuilder;
    }

    private static void initClientAndRequest(PushCenterFeishuBuilder messageBuilder) {
        PushCenterProperties pushCenterProperties = PlmContextHolder.getBean(PushCenterProperties.class);
        if (Objects.isNull(client)) {
            synchronized (PushCenterFeishuBuilder.class) {
                if (Objects.isNull(client)) {
                    client = new DefaultTransClient(pushCenterProperties.getGateway(), pushCenterProperties.getAppKey(), pushCenterProperties.getAppSecret());
                }
            }
        }
        SendIMRequest request = new SendIMRequest(ChannelType.FEI_SHU.getChannel());
        request.setTenantKey(pushCenterProperties.getAppKey());
        request.setMsgType(MsgType.CARD.getType());
        request.setSender(pushCenterProperties.getFeishuAppId());
        request.setContent(CardBody.builder().config(Config.builder().enableForward(true).updateMulti(true).build()).build());
        messageBuilder.setSendIMRequest(request);
    }

    private SendIMRequest getSendIMRequest() {
        return sendIMRequest;
    }

    private void setSendIMRequest(SendIMRequest sendIMRequest) {
        this.sendIMRequest = sendIMRequest;
    }

    /**
     * 飞书卡片文本内容
     * @param content 通知内容
     * @return
     */
    public PushCenterFeishuBuilder content(String content) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = CollectionUtils.isNotEmpty(cardBody.getElements()) ? cardBody.getElements() : Lists.newArrayList();
        Div testMessage = Div.builder().fields(Lists.newArrayList(Field.builder().text(Text.builder().content(content).build()).build())).build();
        elements.add(testMessage);
        cardBody.setElements(elements);
        return this;
    }


    /**
     * 飞书卡片文本内容
     * @param content 通知内容
     * @return
     */
    public PushCenterFeishuBuilder content(List<String> content) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        content.forEach(e ->{
            Div testMessage = Div.builder().fields(Lists.newArrayList(Field.builder().text(Text.builder().content(e).build()).build())).build();
            elements.add(testMessage);
        });
        cardBody.setElements(elements);
        return this;
    }

/**
     * 多列卡片消息内容
     * @param content 通知内容
     * @return
     */
    public PushCenterFeishuBuilder contentWithColumnSet(List<String> content) {
        List<Column> columns = new ArrayList<>();
        content.forEach(e ->{
            columns.add(Column.builder().width("auto").elements(Lists.newArrayList(Markdown.builder().content(e).build())).build());
        });
        contentWithColumns(columns);
        return this;
    }

    /**
     * 多列卡片消息内容
     * @param columns 通知内容
     * @return
     */
    public PushCenterFeishuBuilder contentWithColumns(List<Column> columns) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        elements.add(ColumnSet.builder().background("grey").columns(columns).build());
        cardBody.setElements(elements);
        return this;
    }

    /**
     * 飞书卡片文本内容带分割线
     * @param content
     * @return
     */
    public PushCenterFeishuBuilder contentWithDividingLine(List<String> content) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        content.forEach(e ->{
            Div testMessage = Div.builder().fields(Lists.newArrayList(Field.builder().text(Text.builder().content(e).build()).build())).build();
            elements.add(testMessage);
            elements.add(new HorizontalRule());
        });
        cardBody.setElements(elements);
        return this;
    }

    /**
     * 飞书卡片添加 图片
     * @param url 图片链接
     * @return
     */
    public PushCenterFeishuBuilder image(String head, String url){
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        elements.add(Image.builder().alt(Text.builder().content(head).build()).url(url).mode(ImageMode.FIT_HORIZONTAL.getValue()).build());
        cardBody.setElements(elements);
        return this;
    }

    /**
     * 飞书卡片 分割线
     * @return
     */
    public PushCenterFeishuBuilder dividingLine(){
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        elements.add(new HorizontalRule());
        cardBody.setElements(elements);
        return this;
    }

    /**
     * 飞书卡片文本内容中的按钮
     * @param buttons 按钮
     * 如果需要设置不用样式，请参照 https://open.feishu.cn/document/uYjL24iN/uIjNuIjNuIjN 说明
     * @return
     */
    public PushCenterFeishuBuilder action(List<Button> buttons) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        List<Module> elements = Optional.ofNullable(cardBody.getElements()).orElse(Lists.newArrayList());
        Action action = new Action();
        action.setType(ElementType.ACTION.getValue());
        List<Interactive> interActives = Lists.newArrayList();
        interActives.addAll(buttons);
        action.setActions(interActives);
        elements.add(action);
        cardBody.setElements(elements);
        return this;
    }

    /**
     * 飞书卡片标题
     * @param title
     * @return
     */
    public PushCenterFeishuBuilder title(String title) {
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        cardBody.setHeader(Header.builder().title(title).background("blue").build());
        return this;
    }

    /**
     * 飞书卡片标题 （支持自定义颜色）
     * @param title
     * @param backgroundColor
     * @return
     */
    public PushCenterFeishuBuilder title(String title, String backgroundColor){
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        cardBody.setHeader(Header.builder().title(title).background(backgroundColor).build());
        return this;
    }

    public PushCenterFeishuBuilder url(String url) {
        if(StringUtils.isBlank(url)){
            return this;
        }
        CardBody cardBody = (CardBody) sendIMRequest.getContent();
        cardBody.setCardLink(CardLink.builder().url(url).build());
        return this;
    }

    /**
     * 添加主送人
     * @param receivers
     * @return
     */
    public PushCenterFeishuBuilder receivers(List<String> receivers) {
        if (CollectionUtils.isEmpty(receivers)) {
            return this;
        }
        Receiver receiver = Receiver.builder().idType(IdType.EMP_NO.getType()).ids(receivers).build();
        List<Receiver> receiversAll = addReceiver(receiver);
        return this;
    }

    /**
     * 添加主送人
     * @param receivers
     * @return
     */
    public PushCenterFeishuBuilder receiverUserIds(List<String> receivers) {
        if (CollectionUtils.isEmpty(receivers)) {
            return this;
        }
        Receiver receiver = Receiver.builder().idType(IdType.USER_ID.getType()).ids(receivers).build();
        List<Receiver> receiversAll = addReceiver(receiver);
        return this;
    }

    public PushSendResult send() {
        try {
            PushCenterProperties pushCenterProperties = PlmContextHolder.getBean(PushCenterProperties.class);
            if (!CommonConst.SWITH_ON.equalsIgnoreCase(pushCenterProperties.getSendMailSwitch())) {
                log.info("发送邮件开关未开启");
                PushSendResult.builder().success(false).message("发送邮件开关未开启");
            }
            log.info("begin send message : {}", JSON.toJSONString(sendIMRequest));
            SendResponse response = client.execute(sendIMRequest);
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
        List<Receiver> receivers = CollectionUtils.isEmpty(sendIMRequest.getReceivers()) ? Lists.newArrayList() : sendIMRequest.getReceivers();
        receivers.add(receiver);
        sendIMRequest.setReceivers(receivers);
        return receivers;
    }
}
