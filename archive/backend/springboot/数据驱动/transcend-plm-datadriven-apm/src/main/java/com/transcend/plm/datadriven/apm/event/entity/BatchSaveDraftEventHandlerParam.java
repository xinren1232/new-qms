package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

import java.util.List;

/**
 * @author bin.yin
 * @description: 批量暂存事件入参
 * @version:
 * @date 2024/06/17 11:43
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchSaveDraftEventHandlerParam extends EventHandlerParam {
    private List<MVersionObject> draftDataList;
}
