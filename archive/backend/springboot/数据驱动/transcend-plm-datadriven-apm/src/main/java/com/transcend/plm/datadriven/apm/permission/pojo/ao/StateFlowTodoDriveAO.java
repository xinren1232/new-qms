package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;
import org.elasticsearch.action.search.SearchTask;

import java.util.List;
import java.util.Set;

/**
 * @author unknown
 */
@Data
public class StateFlowTodoDriveAO {
    private String instanceBid;
    private String instanceName;
    private String lifeCycleCode;
    private Set<String> personResponsible;
    private String spaceBid;
    private String spaceAppBid;
    private String modelCode;
    private Boolean isLastState;

}
