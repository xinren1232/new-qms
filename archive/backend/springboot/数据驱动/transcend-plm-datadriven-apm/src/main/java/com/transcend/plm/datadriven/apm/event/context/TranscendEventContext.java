package com.transcend.plm.datadriven.apm.event.context;

/**
 * @author bin.yin
 * @description: transcend事件上下文
 * @version:
 * @date 2024/08/26 19:20
 */
public class TranscendEventContext {

    private static final ThreadLocal<EventData<?>> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();


    public static void set(EventData<?> eventData) {
        CONTEXT_THREAD_LOCAL.set(eventData);
    }

    public static <T> EventData<T> get() {
        return (EventData<T>) CONTEXT_THREAD_LOCAL.get();
    }

    public static void clear() {
        CONTEXT_THREAD_LOCAL.remove();
    }
}
