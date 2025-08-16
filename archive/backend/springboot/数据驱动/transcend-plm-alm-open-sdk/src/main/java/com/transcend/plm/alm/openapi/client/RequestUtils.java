package com.transcend.plm.alm.openapi.client;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 请求工具
 *
 * @author xin.wu2
 * @version v1.0
 * createdAt 2025/5/21
 */
@NoArgsConstructor
public class RequestUtils {

    /**
     * 获取字段值
     *
     * @param obj   解析对象
     * @param field 对应字段
     * @return 字段信息
     */
    static Object getFieldValue(Object obj, Field field) {
        Object value = ReflectUtil.getFieldValue(obj, field);
        if (value != null) {
            if (value instanceof Enum) {
                return getEnumValue(value);
            } else if (value instanceof Date || value instanceof LocalDateTime) {
                JsonFormat format = field.getAnnotation(JsonFormat.class);
                if (format != null && format.pattern() != null) {
                    if (value instanceof Date) {
                        return DateUtil.format((Date) value, format.pattern());
                    } else {
                        return DateUtil.format((LocalDateTime) value, format.pattern());
                    }
                }
                return DatePattern.NORM_DATETIME_FORMAT.format(value);
            }
        }
        return value;
    }

    /**
     * 获取枚举值信息
     *
     * @param value 值类型
     * @return 枚举值结果
     */
    static Object getEnumValue(Object value) {
        Field[] fields = ReflectUtil.getFields(value.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(JsonValue.class)) {
                return ReflectUtil.getFieldValue(value, field);
            }
        }
        return String.valueOf(value);
    }


    /**
     * 获取参数名称
     *
     * @param field 字段信息
     * @return 参数名称
     */
    static String getFieldName(Field field) {
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        if (annotation != null) {
            return annotation.value();
        }
        return field.getName();
    }

    /**
     * 获取参数map
     *
     * @param params 参数信息
     * @return 参数map
     */
    static Map<String, String> paramMap(Object params) {
        if (params == null) {
            return Collections.emptyMap();
        }
        Field[] fields = ReflectUtil.getFields(params.getClass(),
                field -> !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())));
        Map<String, String> paramMap = new TreeMap<>();
        for (Field field : fields) {
            Object value = getFieldValue(params, field);
            if (value != null) {
                if (value instanceof String || ObjectUtil.isBasicType(value)) {
                    paramMap.put(getFieldName(field), String.valueOf(value));
                }
            }
        }
        return paramMap;
    }

    /**
     * 拼接url
     *
     * @param domain 域名
     * @param uri    访问uri
     * @return 拼接结果
     */
    static String joinUrl(@NonNull String domain, @NonNull String uri) {
        uri = uri.trim();
        domain = domain.trim();
        String separator = "/";
        boolean isSeparator = domain.endsWith(separator) || uri.startsWith(separator);
        return domain + (isSeparator ? "" : separator) + uri;
    }


    /**
     * 拼接map参数
     *
     * @param paramMap 参数map
     * @return 参数拼接结果
     */
    static String joinMapParams(@NonNull Map<String, String> paramMap) {
        return paramMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

}
