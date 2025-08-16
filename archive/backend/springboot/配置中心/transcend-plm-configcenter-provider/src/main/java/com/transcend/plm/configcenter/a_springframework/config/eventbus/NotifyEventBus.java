package com.transcend.plm.configcenter.a_springframework.config.eventbus;

import com.transcend.plm.configcenter.common.pojo.dto.NotifyEventBusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.transsion.framework.tool.eventbus.EventBusWapper;
import com.transsion.framework.tool.eventbus.EventIdentifier;

/**
 * https://zhuanlan.zhihu.com/p/336469154
 * https://www.cnblogs.com/xd502djj/p/14436582.html
 * 事件总线
 * 1、NotifyEventBusService  注册事件订阅方法，如deliveryIMEI
 * 2、NotifyEventBus         发送事件，  如postDeliveryIMEI
 * 3、注册事件订阅方法和发送事件是通过事件类型处理粘性事件，所以通知和接受事件类型必须一致，一个事件只能有一个同类型参数
 * @author zhihui.yu
 *
 */
public class NotifyEventBus
{
	static Logger log = LoggerFactory.getLogger(NotifyEventBus.class);
	private static EventBusWapper eventBus;
	static {
		eventBus = EventBusWapper.getInstance(EventIdentifier.builder()
				.name("NOTIFY")
				.poolSize(5)
				.build());
	}
	public static void register(Object object){
		eventBus.register(object);
	}
	
	/*
	 * 发起事件
	 */
	public static void publishEvent(NotifyEventBusDto notifyEventBusDto) {
		eventBus.post(notifyEventBusDto);
		
	}
}
