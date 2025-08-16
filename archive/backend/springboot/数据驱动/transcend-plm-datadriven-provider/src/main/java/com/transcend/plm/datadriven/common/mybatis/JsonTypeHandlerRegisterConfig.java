package com.transcend.plm.datadriven.common.mybatis;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 注册JSON类型处理器
 * @createTime 2023-08-30 14:51:00
 */
@Configuration
public class JsonTypeHandlerRegisterConfig implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        SqlSessionFactory sqlSessionFactory = event.getApplicationContext().getBean(SqlSessionFactory.class);
        sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(JSON.class, null, FastjsonTypeHandler.class);
        sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(List.class, null, FastjsonTypeHandler.class);
        sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ArrayList.class, null, FastjsonTypeHandler.class);
    }
}
