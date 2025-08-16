package com.transcend.plm.datadriven.api.model.relation.delete;

import lombok.Data;

/**
 * @author bin.yin
 * @date 2024/05/14 11:35
 */
@Data
public class StructureRelData {
    private String bid;
    private String dataBid;
    private String dataSource;
    private String parentBid;
    private String parentDataBid;
    private String parentDataSource;
}
