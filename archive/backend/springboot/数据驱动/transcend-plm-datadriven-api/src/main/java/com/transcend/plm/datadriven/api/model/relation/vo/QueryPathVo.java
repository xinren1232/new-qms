package com.transcend.plm.datadriven.api.model.relation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bin.yin
 * @description: cad查询路径vo
 * @version:
 * @date 2024/08/26 17:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryPathVo {

    private String dataBid;

    private List<String> path;
}
