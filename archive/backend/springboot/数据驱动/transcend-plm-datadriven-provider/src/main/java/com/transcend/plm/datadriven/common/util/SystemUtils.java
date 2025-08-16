package com.transcend.plm.datadriven.common.util;


import org.apache.commons.lang3.StringUtils;

/**
 * @author qihua.sun
 * @date 2024/07/24
 */
public class SystemUtils {

    /**
     *
     */
    private SystemUtils(){}

    /**
     * @param envName
     * @param defaultValue
     * @return {@link String }
     */
    public static String getEnv(String envName,String defaultValue)
    {
        String envValue=null;
        envValue=System.getenv(envName);
        if(!StringUtils.isBlank(envValue))
        {
            return envValue;
        }
        envValue=System.getProperty( envName);
        if(!StringUtils.isBlank(envValue))
        {
            return envValue;
        }
        return defaultValue;
    }

    /**
     * @param envName
     * @return {@link String }
     */
    public static String getEnv(String envName)
    {
       return getEnv(envName,null);
    }
}
