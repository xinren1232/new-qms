package com.transcend.plm.configcenter.common.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//@EnableWebMvc
//@ComponentScan(basePackages = "com.transcend.plm.configcenter")
public class AppConfig implements WebMvcConfigurer {

//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        // 注册自定义的数据绑定器
//        binder.initDirectFieldAccess();
//        binder.registerCustomEditor(Integer.class, new EnableFlagEditor());
//    }

//    @Bean
//    public MyWebBindingInitializer myWebBindingInitializer() {
//        return new MyWebBindingInitializer();
//    }
}
