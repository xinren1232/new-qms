package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

/**
 * @author bin.yin
 * @description: 修订事件入参
 * @version:
 * @date 2024/06/17 13:40
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviseEventHandlerParam extends EventHandlerParam {
    private String bid;
}
