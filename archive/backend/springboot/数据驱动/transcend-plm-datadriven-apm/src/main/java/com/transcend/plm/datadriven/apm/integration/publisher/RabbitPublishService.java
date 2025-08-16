package com.transcend.plm.datadriven.apm.integration.publisher;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description RabbitMQ推送服务
 * @createTime 2023-12-18 17:39:00
 */
@Service
public class RabbitPublishService implements IPublishService {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Override
    public void publish(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @Override
    public void publishWithDelay(String exchange, String routingKey, Object message, Long delayTime) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setHeader("x-delay", delayTime);
                return message;
            }
        });
    }


    @Override
    public void publish(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(routingKey, message);
    }
}
