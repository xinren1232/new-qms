package com.transcend.plm.datadriven.apm.space.event;

import com.transcend.plm.datadriven.apm.space.pojo.bo.FieldUpdateEvent;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 字段更新事件处理
 * @createTime 2023-11-02 11:44:00
 */
public interface IFieldUpdateEventHandler {
    /**
     * 处理字段更新事件
     *
     * @param fieldUpdateEvent 字段更新事件
     */
    void handleFieldUpdateEvent(final FieldUpdateEvent fieldUpdateEvent);

    /**
     * 是否匹配
     *
     * @param fieldUpdateEvent fieldUpdateEvent
     * @return {@link boolean}
     */
    default boolean match(final FieldUpdateEvent fieldUpdateEvent) {
        return true;
    }

    /**
     * 获取处理器名称
     *
     * @return {@link String}
     */
    default String getHandlerName() {
        return this.getClass().getSimpleName();
    }
}


