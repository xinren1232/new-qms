package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

/**
 * @author bin.yin
 * @description: 检入事件入参
 * @version:
 * @date 2024/06/13 17:33
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckInEventHandlerParam extends EventHandlerParam {

    private MVersionObject mObject;
}
