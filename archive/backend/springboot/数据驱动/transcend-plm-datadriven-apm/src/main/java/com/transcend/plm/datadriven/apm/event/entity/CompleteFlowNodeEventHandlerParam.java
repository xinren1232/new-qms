package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.*;

/**
 * @author haijun.ren
 * @description: 完成节点事件参数
 * @version:
 * @date 2024/09/04 14:17
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteFlowNodeEventHandlerParam extends EventHandlerParam {
    private String nodeBid;
    private String bid;
    private MSpaceAppData mSpaceAppData;
}
