package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;

@Data
public class SourceModelCodeInfo {
    private String bid;
    private String modelCode;
    private String name;
    private String baseModel;
    private String spaceAppBid;
}
