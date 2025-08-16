package com.transcend.plm.datadriven.api.model.relation.qo;

import lombok.Data;

/**
 * @author bin.yin
 * @date 2024/05/13 09:33
 */
@Data
public class RelationParentQo {
    private String targetBid;
    private String targetDataBid;
    private String relationModelCode;
    private String dataSource;
}
