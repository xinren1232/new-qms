package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

/**
 * @author bin.yin
 * @description: 取消检出事件入参
 * @version:
 * @date 2024/06/17 10:41
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelCheckOutEventHandlerParam extends EventHandlerParam {
    private String bid;
}
