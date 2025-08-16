package com.transcend.plm.alm.demandmanagement.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * 节假日类型
 *
 * @author haidong.liu
 */
public enum CalendarTypeEnum implements BaseEnum<String> {

    HOLIDAY("3", "法定假"),
    WEEKLY_REST("2", "周休"),
    WORKDAYS("1", "正班"),


    ;

    /**
     * code
     */
    String code;
    /**
     * desc
     */
    String desc;

    CalendarTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
