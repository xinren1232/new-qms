package com.transcend.plm.datadriven.common.util;

/**
 * @author sgx
 * @date
 */
public class StringToDoubleUtil {
    /**
     * 将字符串转换为double类型，如果转换失败，返回默认值0.0
     *
     * @param str 待转换的字符串
     * @return 转换后的double值，如果转换失败返回0.0
     */
    public static double stringToDouble(String str) {
        return stringToDouble(str, 0.0);
    }

    /**
     * 将字符串转换为double类型，如果转换失败，返回指定的默认值
     *
     * @param str          待转换的字符串
     * @param defaultValue 转换失败时返回的默认值
     * @return 转换后的double值，如果转换失败返回defaultValue
     */
    public static double stringToDouble(String str, double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
