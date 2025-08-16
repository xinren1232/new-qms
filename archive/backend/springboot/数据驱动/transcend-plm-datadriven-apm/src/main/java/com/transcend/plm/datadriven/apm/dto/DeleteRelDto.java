package com.transcend.plm.datadriven.apm.dto;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class DeleteRelDto {
    private String sourceBid;
    private List<String> targetBids;

    private String modelCode;
}
