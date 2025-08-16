package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 回退流程实例参数对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/3 10:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RollbackFlowNodeEventHandlerParam extends EventHandlerParam {

    private String bid;
    private String nodeBid;
    private MSpaceAppData mSpaceAppData;
    private boolean runEvent;

}
