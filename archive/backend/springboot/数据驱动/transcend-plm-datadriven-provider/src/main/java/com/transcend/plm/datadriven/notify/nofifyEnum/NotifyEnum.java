package com.transcend.plm.datadriven.notify.nofifyEnum;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象版本模型配置常量
 *
 * @author jie.luo1
 */
public enum NotifyEnum implements BaseEnum<String> {
    /**
     *通知时间类型-实时
     */
    TIME_TYPE_NOW("NOW", "通知时间类型-实时"),
    /**
     *通知时间类型-指定时间
     */
    TIME_TYPE_SPECIFY_TIME("SPECIFY", "通知时间类型-指定时间"),
    /**
     *通知时间类型-业务时间
     */
    TIME_TYPE_BUSINESS("BUSINESS", "通知时间类型-业务时间"),
    /**
     *通知时间类型-天
     */
    TIME_TYPE_DAY("DAY", "通知时间类型-天"),
    /**
     *通知时间类型-周
     */

    TIME_TYPE_WEEK("WEEK", "通知时间类型-周"),
    /**
     *通知时间类型-小时
     */

    TIME_TYPE_HOUR("HOUR", "通知时间类型-小时"),
    /**
     *通知时间类型-月
     */

    TIME_TYPE_MONTH("MONTH", "通知时间类型-月"),
    /**
     *通知时间类型-间隔时间
     */

    TIME_TYPE_INTERVAL("INTERVAL", "通知时间类型-间隔时间"),
    /**
     *通知时间类型-分
     */

    TIME_TYPE_MINUTE("MINUTE", "通知时间类型-分"),
    /**
     *通知时间类型-秒
     */

    TIME_TYPE_SECOND("SECOND", "通知时间类型-秒"),
    /**
     *通知类型-对象类型
     */
    BIZ_TYPE_OBJECT("OBJECT", "通知类型-对象类型"),
    /**
     *通知类型-应用类型
     */

    BIZ_TYPE_APP("APP", "通知类型-应用类型"),


    /**
     * 通知类型-实例类型
     */
    BIZ_TYPE_INSTANCE("INSTANCE", "通知类型-实例类型"),
    /**
     *操作类型
     */

    OPERATE_TYPE("1", "操作类型"),
    /**
     *创建实例
     */

    OPERATE_CREATE("CREATE", "创建实例"),
    /**
     *修改实例
     */

    OPERATE_UPDATE("UPDATE", "修改实例"),
    /**
     *修改关联属性
     */

    OPERATE_UPDATE_REL("UPDATE_REL", "修改关联属性"),
    /**
     *添加及删除直接子关系
     */

    OPERATE_CREATE_OR_DELETE_REL("CREATE_OR_DELETE_REL", "添加及删除直接子关系"),
    /**
     *触发类型
     */
    TRIGGER_TYPE("2", "触发类型");


    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    NotifyEnum(String code, String desc) {
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
