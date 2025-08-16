package com.transcend.plm.datadriven.api.model.relation.delete;

import lombok.Data;

import java.util.Set;

/**
 * @author bin.yin
 * @date 2024/05/14 11:33
 */
@Data
public class StructureRelDel {
    /**
     * 当前详情的bid
     */
    private String currentBid;

    private String relationModelCode;

    private Set<String> delRelPathSet;
}
