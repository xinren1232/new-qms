package com.transcend.plm.datadriven.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 应用启动后执行
 *
 * @author zhihui.yu
 * @date 2024/07/24
 */
@Slf4j
@Component
public class ApplicationRunnerStarter implements ApplicationRunner{


	@Override
	public void run(ApplicationArguments args) throws Exception {
		//调用服务方法进行缓存预热
		
		
	}
}
