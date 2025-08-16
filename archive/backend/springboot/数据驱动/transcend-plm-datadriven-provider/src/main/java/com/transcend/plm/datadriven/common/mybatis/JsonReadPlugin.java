package com.transcend.plm.datadriven.common.mybatis;

import com.alibaba.fastjson.JSONObject;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.constant.TableTypeConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author unknown
 * 通过拦截器对返回结果中的某个字段进行加密处理
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
public class JsonReadPlugin implements Interceptor {


    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) target;
        // 获取resultSetHandler的元数据
        MetaObject metaResultSetHandler = MetaObject.forObject(resultSetHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
        // 获取入参
        ParameterHandler parameterHandler = (ParameterHandler) metaResultSetHandler.getValue("parameterHandler");
        Object parameterObject = parameterHandler.getParameterObject();
        TableDefinition tableDefinition = null;
        if (parameterObject instanceof Map) {
            Map paramMap = (Map) parameterObject;
            for (Object value : paramMap.values()) {
                if (value instanceof TableDefinition) {
                    tableDefinition = (TableDefinition) value;
                    break;
                }
            }
        }
        // 没有表定义直接退出
        if (tableDefinition == null) {
            return invocation.proceed();
        }
        Set<String> jsonFields = tableDefinition.getTableAttributeDefinitions().stream().filter(attributeDefinition -> TableTypeConstant.JSON.equals(attributeDefinition.getType()))
                .map(TableAttributeDefinition::getProperty).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(jsonFields)){
            return invocation.proceed();
        }
        Object result = invocation.proceed();
        // 1.匹配结果集list
        if (result instanceof List) {
            // 1.匹配MBaseData
            List list = (List) result;
            list.forEach(m -> {
                        if (m instanceof MBaseData) {
                            MBaseData data = (MBaseData)m;
                            jsonFields.forEach(jsonField->{
                                Object value = data.get(jsonField);
                                if (value instanceof String){
                                    // 字符转换为json
                                    Object jsonObject = JSONObject.parse((String)value);
                                    data.put(jsonField, jsonObject);
                                }
                            });
                        }
                    }

            );
        }
        return result;
    }


}
