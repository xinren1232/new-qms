package com.transcend.plm.datadriven.apm.integration.publisher;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 推送服务
 * @createTime 2023-12-18 17:30:00
 */
public interface IPublishService {
    /**
     * 推送消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     */
    void publish(String exchange, String routingKey, Object message);

    /**
     * 推送消息 （延迟时间生效的前提是发送到x-delayed-message类型的队列）
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     * @param delayTime  延迟时间 单位毫秒
     */
    void publishWithDelay(String exchange, String routingKey, Object message, Long delayTime);

    /**
     * publish
     *
     * @param routingKey routingKey
     * @param message    message
     */
    void publish(String routingKey, Object message);
}
