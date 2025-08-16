package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/14
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogicalDeleteEventHandlerParam extends EventHandlerParam {

    private String bid;
}
