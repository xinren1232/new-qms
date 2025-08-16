package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmAppTabHeaderDto {
    private String bizBid;

    private String code;

    private List<Map> configContent;
}
