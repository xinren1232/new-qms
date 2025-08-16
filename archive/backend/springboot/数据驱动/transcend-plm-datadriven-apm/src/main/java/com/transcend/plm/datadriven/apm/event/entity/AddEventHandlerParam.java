package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.*;

/**
 * @author bin.yin
 * @description: 新增实例事件参数
 * @version:
 * @date 2024/06/13 15:32
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddEventHandlerParam extends EventHandlerParam {
    private MSpaceAppData mSpaceAppData;
    private ApmSpaceAppDataDrivenOperationFilterBo filterBo;
    /**
     * 草稿数据变成正式数据
     */
    private String draftBid;
}
