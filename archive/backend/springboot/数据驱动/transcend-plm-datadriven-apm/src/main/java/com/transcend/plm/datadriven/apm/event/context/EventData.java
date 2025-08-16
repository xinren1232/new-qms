package com.transcend.plm.datadriven.apm.event.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/08/26 19:22
 */
@Getter
@Setter
@ToString
public class EventData<T> {
    private T data;
    public EventData(T data) {
        this.data = data;
    }
}
