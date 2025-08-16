package com.transcend.plm.datadriven.api.model.relation.qo;

import lombok.Data;

import java.util.List;

/**
 * @author bin.yin
 * @description: cad查询路径qo
 * @version:
 * @date 2024/08/26 17:26
 */
@Data
public class QueryPathQo {
    private String sourceBid;

    private String relationModelCode;

    private List<String> queryDataBids;
}
