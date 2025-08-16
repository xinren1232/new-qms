//package com.transcend.plm.configcenter.application.service.websocket;
//
//import org.springframework.stereotype.Service;
//
//import com.transsion.framework.common.JsonUtil;
//import com.transsion.framework.ext.websocket.handler.WebSocketUtil;
//import com.transsion.framework.ext.websocket.message.AbstractWebSocketMessageConverter;
//import com.transsion.framework.ext.websocket.message.WebSocketMessage;
//
//import lombok.extern.slf4j.Slf4j;
//
///*
// * socket消息发送处理示例
// */
//@Slf4j
//@Service
//public class WebSocketMessageImpl extends AbstractWebSocketMessageConverter{
//
//	/*
//	 * 和前端发送的code一致
//	 */
//	@Override
//	public String getCode() {
//		return "message";
//	}
//
//	@Override
//	public void process(String socketId,Object data) {
//		log.info("########### websocket消息处理服务 ###########");
//		String json=JsonUtil.toString(data);
//		log.info(json);
//
//		WebSocketMessage<String> sm=WebSocketMessage.of(socketId,"message_finish","消息处理完成");
//		WebSocketUtil.sendMessage(sm);
//	}
//}
