package com.transcend.plm.datadriven.apm.task.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmTaskDeleteAO {
    private String taskType;
    private List<String> bizBids;
}
