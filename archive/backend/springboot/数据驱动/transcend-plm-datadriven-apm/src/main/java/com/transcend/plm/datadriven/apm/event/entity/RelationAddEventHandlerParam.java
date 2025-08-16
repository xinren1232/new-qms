package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.RelationEventHandlerParam;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import lombok.*;

/**
 * @author bin.yin
 * @date 2024/06/13 17:54
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationAddEventHandlerParam extends RelationEventHandlerParam {
    private String source;
    private AddExpandAo addExpandAo;
}
