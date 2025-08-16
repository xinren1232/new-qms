package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class MultiAppTreeConfig {
    private String sourceModelCode;
    private MultiAppTreeConfig targetModelCode;
    private String relationModelCode;
}
