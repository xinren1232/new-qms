package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowLineEvent;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class StateDataDriveAO {
    private boolean isStartWorkFlow = true;
    private String instanceBid;
    private ApmFlowLineEvent apmFlowLineEvent;
    private String lifeCycleCode;
    private String spaceBid;
    private String modelCode;

}
