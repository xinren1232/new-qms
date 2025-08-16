package com.transcend.plm.datadriven.apm.integration.message;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 队列名称常量
 * @createTime 2023-12-19 10:58:00
 */
public class QueueNameConstant {
    private QueueNameConstant() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 实例操作同步队列
     */
    public static final String INSTANCE_OPERATE_SYNCHRONIZE = "/instance/operate/synchronize";

    /**
     * 实例操作同步失败死信队列
     */
    public static final String INSTANCE_DEAD_SYNCHRONIZE_FAIL = "/instance/dead/synchronize/fail";

    /**
     * 实例操作事件内部应用通知队列
     */
    public static final String INSTANCE_OPERATE_EVENT_SELF = "/instance/operate/event/self";
    /**
     * 实例操作事件外部应用通知队列
     */
    public static final String INSTANCE_OPERATE_EVENT_FOREIGN = "/instance/operate/event/foreign";
    /**
     * 实例操作事件交换机
     */
    public static final String EXCHANGE_INSTANCE_OPERATE = "/instance/operate";


    public static final String EXCHANGE_INSTANCE_DELAY = "/instance/delay";

    /**
     * 实例操作通知绑定键
     */
    public static final String ROUTING_KEY_INSTANCE_OPERATE_EVENT = "/instance/operate/event";
    /**
     * 实例操作同步绑定键
     */
    public static final String ROUTING_KEY_INSTANCE_OPERATE_SYNCHRONIZE = "/instance/operate/synchronize";

    /**
     * 实例操作同步失败绑定键
     */
    public static final String ROUTING_KEY_INSTANCE_OPERATE_SYNCHRONIZE_FAIL = "/instance/operate/synchronize/fail";

    public static final String ROUTING_KEY_DELAY = "delayQueue";

    public static final String QUEUE_DELAY = "delayQueue";
}
