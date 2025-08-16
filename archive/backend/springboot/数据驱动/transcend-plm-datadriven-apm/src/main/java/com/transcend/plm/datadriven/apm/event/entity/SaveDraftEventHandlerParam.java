package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

/**
 * @author bin.yin
 * @description: 暂存事件入参
 * @version:
 * @date 2024/06/17 11:43
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveDraftEventHandlerParam extends EventHandlerParam {
    private MVersionObject draftData;
}
