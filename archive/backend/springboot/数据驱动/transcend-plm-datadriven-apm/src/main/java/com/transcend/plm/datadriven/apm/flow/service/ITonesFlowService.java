package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.api.model.MObject;

import java.util.List;

/**
 * @author unknown
 */
public interface ITonesFlowService {
    /**
     * listTonesObject
     *
     * @return {@link List<MObject>}
     */
    List<MObject> listTonesObject();

    /**
     * handleTonesFlowNodes
     *
     * @param mObject mObject
     * @return {@link boolean}
     */
    boolean handleTonesFlowNodes(MObject mObject);

    /**
     * handleNodeState
     *
     * @return {@link boolean}
     */
    boolean handleNodeState();

    /**
     * handleNodeHandler
     *
     * @return {@link boolean}
     */
    boolean handleNodeHandler();
}
