package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;

@Data
public class RelationInfo {
    private String bid;
    private String name;
    private String type;
    private String tabName;
    private String modelCode;
    private String sourceModelCode;
    private String targetModelCode;
}
