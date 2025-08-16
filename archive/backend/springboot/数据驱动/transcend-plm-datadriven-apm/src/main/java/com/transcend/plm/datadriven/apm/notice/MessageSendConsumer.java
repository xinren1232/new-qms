package com.transcend.plm.datadriven.apm.notice;

import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023-11-08 14:53:00
 */
@Component
public class MessageSendConsumer implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyEventBus.register(this);
    }

    @Subscribe
    public void send(PushCenterFeishuBuilder feishuBuilder) {
        feishuBuilder.send();
    }
}
