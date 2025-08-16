package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class MultiAppConfigDto {
    private String name;
    private String bid;
    private String parentModelCode;
    private String modelCode;
    private boolean firstLevel;
}
