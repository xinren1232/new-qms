//package com.transcend.plm.configcenter.config.eventbus;
//
//import com.transcend.plm.configcenter.common.pojo.dto.NotifyEventBusDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.google.common.eventbus.Subscribe;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// *
// * 用户订阅事件订阅服务
// * @author zhihui.yu
// *
// */
//@Slf4j
//@Service
//public class NotifyEventBusService {
//	/**
//	 * 注册对象订阅抛出事件
//	 */
//	public NotifyEventBusService(){
//		NotifyEventBus.register(this);
//	}
//
//	@Autowired
//    ISampleService sampleService;
//	/*
//	 * 订阅事件
//	 */
//	@Subscribe
//	public void subscribeEvent(NotifyEventBusDto notifyEventBusDto) {
//		//此处不捕捉异常，将有全局异常捕捉机制，保证线程安全
//		sampleService.eventBusTest(notifyEventBusDto);
//
//		/*
//		try {
//			sampleService.eventBusTest(notifyEventBusDto);
//		}
//		catch(BusinessException be) {
//			log.error("事件订阅过程发生业务异常",be);
//		}
//		*/
//	}
//}
