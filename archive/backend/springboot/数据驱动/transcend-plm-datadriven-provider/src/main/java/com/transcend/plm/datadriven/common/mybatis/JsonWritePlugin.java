package com.transcend.plm.datadriven.common.mybatis;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.MybatisParameterHandler;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.constant.TableTypeConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 处理json类型的插件
 * @createTime 2023-08-30 11:05:00
 */
@Intercepts({
        @Signature(
                type = ParameterHandler.class,
                method = "setParameters",
                args = {PreparedStatement.class}
        )
}
)
public class JsonWritePlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (!(target instanceof MybatisParameterHandler)) {
            return invocation.proceed();
        }
        MybatisParameterHandler parameterHandler = (MybatisParameterHandler) target;
        Object parameterObject = parameterHandler.getParameterObject();
        TableDefinition tableDefinition = null;
        if (parameterObject instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) parameterObject;
            for (Object value : paramMap.values()) {
                if (value instanceof TableDefinition) {
                    tableDefinition = (TableDefinition) value;
                    break;
                }
            }
        }
        if (tableDefinition == null) {
            return invocation.proceed();
        }
        Set<String> jsonFields = tableDefinition.getTableAttributeDefinitions().stream().filter(attributeDefinition -> TableTypeConstant.JSON.equals(attributeDefinition.getType()))
                .map(TableAttributeDefinition::getProperty).collect(Collectors.toSet());
        if (jsonFields.isEmpty()) {
            return invocation.proceed();
        }
        //利用反射从parameterHandler获取bounldSql
        BoundSql boundSql = (BoundSql) ReflectUtil.getFieldValue(parameterHandler, "boundSql");
        //利用反射从boundSql获取parameterMappings
        List<ParameterMapping> parameterMappings = (List<ParameterMapping>) ReflectUtil.getFieldValue(boundSql, "parameterMappings");
        if (CollectionUtils.isEmpty(parameterMappings)) {
            return invocation.proceed();
        }
        for (ParameterMapping parameterMapping : parameterMappings) {
            String property = parameterMapping.getProperty();
            String fieldName = property.substring(property.lastIndexOf(".") + 1);
            if (jsonFields.contains(fieldName)) {
                //通过反射修改parameterMapping的typeHandler
                ReflectUtil.setFieldValue(parameterMapping, "typeHandler", new FastjsonTypeHandler(Object.class));
            }
        }
        return invocation.proceed();
    }
}
