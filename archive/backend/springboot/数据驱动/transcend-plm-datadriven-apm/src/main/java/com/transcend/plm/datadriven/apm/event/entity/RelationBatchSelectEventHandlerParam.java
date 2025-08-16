package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.RelationEventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;
import lombok.*;

/**
 * @author bin.yin
 * @description: 关系新增 事件入参
 * @version:
 * @date 2024/06/13 17:54
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationBatchSelectEventHandlerParam extends RelationEventHandlerParam {
    private ApmRelationMultiTreeAddParam relationMultiTreeAddParam;
}
