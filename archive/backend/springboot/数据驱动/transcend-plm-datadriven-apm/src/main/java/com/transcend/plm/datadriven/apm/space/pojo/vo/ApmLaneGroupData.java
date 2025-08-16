package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.datadriven.api.model.MObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 泳道分组数据
 * @createTime 2023-11-04 15:13:00
 */
@Data
public class ApmLaneGroupData {
    private List<String> headers;
    private List<ApmLaneDateVO> laneDatas;
    /**
     * 表头数据
     */
    private Map<String, List<String>> headersMap;
    /**
     * 泳道数据
     */
    private Map<String, List<MObject>> laneAllMObjects;
}
