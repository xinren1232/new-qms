package com.transcend.plm.datadriven.common.util;

import com.transcend.plm.datadriven.api.model.MBaseData;
import org.apache.commons.lang3.StringUtils;

/**
 * 对象工具类
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public class TranscendObjectUtil {
    /**
     * @param object
     * @param fieldName
     * @return boolean
     */
    public static <T extends MBaseData> boolean isFiledEmpty(T object, String fieldName) {
        return object.get(fieldName) == null || StringUtils.isBlank(object.get(fieldName).toString()) || "null".equals(object.get(fieldName).toString());
    }
}
