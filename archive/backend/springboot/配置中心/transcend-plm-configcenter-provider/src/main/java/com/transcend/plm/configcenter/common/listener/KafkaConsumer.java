//package com.transcend.plm.configcenter.listener;
//
//import com.transcend.plm.configcenter.api.model.object.vo.SampleVo;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import com.transsion.framework.common.JsonUtil;
//import com.transsion.hulk.framework.mq.base.DefaultMessage;
//import com.transcend.plm.configcenter.pojo.vo.SampleVo;
//
//import lombok.extern.slf4j.Slf4j;
//
//
///**
// *  一个消费者只能属于一个消费者组
// *  消费者组订阅的topic只能被其中的一个消费者消费
// *  不同消费者组中的消费者可以消费同一个topic
// * @author zhihui.yu
// *
// */
//@Slf4j
//@Component
//public class KafkaConsumer {
//        /**
//         *
//         * kafka的监听器，topic为"zhTest"，消费者组为"zhTestGroup"
//         * @param record
//         * @param ack
//
//    @KafkaListener(topics = "zhTest", groupId = "zhTestGroup")
//    public void listenZhugeGroup(ConsumerRecord<String, DefaultMessage> record, Acknowledgment ack) {
//            log.info("kafka消费者={}",JsonUtil.toJson(record.key()));
//            log.info("kafka消费者={}",JsonUtil.toJson(record.value()));
//        //String value = record.value();
//        //System.out.println(value);
//        //System.out.println(record);
//        //手动提交offset
//        ack.acknowledge();
//    }
//    */
//    @KafkaListener(topics = "test-topic11",id = "test-topic11")
//    public void listenZhugeGroup1(ConsumerRecord<String, String> record) {
//         log.info("kafka消费者1 key={}",record.key());
//         log.info("kafka消费者1 value={}",record.value());
//         //DefaultMessage<SampleVo> message=JsonUtil.parse(record.value(), new TypeReference<DefaultMessage<SampleVo>>() {});
//         //消息体反序列化
//         DefaultMessage<SampleVo> message=DefaultMessage.parse(record.value(),SampleVo.class);
//         log.info("序列化消息实体={}",JsonUtil.toString(message));
//    }
//
//    @KafkaListener(topics = "test-topic22",id = "test-topic22")
//    public void listenZhugeGroup2(ConsumerRecord<String, String> record) {
//         log.info("kafka消费者2 key= {}",record.key());
//         log.info("kafka消费者2 value={}",record.value());
//    }
//}