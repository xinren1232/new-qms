package com.transcend.plm.datadriven.common.constant;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
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
    public static final Boolean BASE_COLUMN_FLAG = Boolean.TRUE;

    /**
     * 扩展基础属性列
     */
    public static final Boolean EXT_COLUMN_FLAG = Boolean.FALSE;

    /**
     * 扩展列
     */
    public static final String COLUMN_EXT = "ext";
    /**
     * 默认父ID
     */
    public static String PROPERTY_PARENT_ID = "parent_bid";
    public static String COLUMN_BID = "bid";

    public static String COLUMN_TARGET_BID = "target_bid";

    public static String COLUMN_SOURCE_BID = "source_bid";
    public static String COLUMN_DATA_BID = "data_bid";

}
