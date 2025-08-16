package com.transcend.plm.configcenter.application.service.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.transsion.framework.common.JsonUtil;
import com.transsion.framework.ext.websocket.handler.WebSocketUtil;
import com.transsion.framework.ext.websocket.message.AbstractWebSocketMessageConverter;
import com.transsion.framework.ext.websocket.message.WebSocketMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * socket文件下载消息处理示例
 * @author zhihui.yu
 *
 */
@Slf4j
@Service
public class WebSocketDownloadImpl extends AbstractWebSocketMessageConverter{

	/*
	 * 和前端发送的code一致
	 */
	@Override
	public String getCode() {
		return "download";
	}

	@Override
	public void process(String socketId,Object data) {
		log.info("########### websocket文件下载服务 ###########");
		String json=JsonUtil.toString(data);
		log.info(json);
		//业务处理完成，向客户端发送消息
		Map<String,String> d=new HashMap<>();
		d.put("download_msg", "文件下载完成");
		
		WebSocketMessage<Map<String,String>> sm=WebSocketMessage.of(socketId,"download_finish",d);
		WebSocketUtil.sendMessage(sm);
	}

}
