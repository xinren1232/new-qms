package com.transcend.plm.configcenter.common.constant;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-03-09 17:37
 **/
public class DataBaseConstant {
    public static final int BATCH_SIZE = 500;

    /**
     * 策略编码（NONE,HASH,COLUMN,TIME）
     */
    public static final String tableStrategyNone = "NONE";

    /**
     * byte类型 TRUE = 1
     */
    public static final Byte BYTE_TRUE = (byte)1;

    /**
     * byte类型 FALSE = 0
     */
    public static final Byte BYTE_FALSE = (byte)0;
    /**
     * 基础属性列
     */
    public static final Byte BASE_COLUMN_FLAG = (byte)1;

    /**
     * 扩展基础属性列
     */
    public static final Byte EXT_COLUMN_FLAG = (byte)0;

    /**
     * 扩展列
     */
    public static final String COLUMN_EXT = "ext";
    /**
     * 默认父ID
     */
    public static String PROPERTY_PARENT_ID = "parentBid";
}
