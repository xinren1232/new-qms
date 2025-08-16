package com.transcend.plm.datadriven.api.model.mata;

import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 自底向上关系跨层级查询Qo
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
@Accessors(chain = true)
public class RelationCrossUpQo {


    @ApiModelProperty("目标实例ID")
    private String targetBid;

    @ApiModelProperty("待查询的源对象表定义")
    private TableDefinition sourceTable;

    @ApiModelProperty("跨层级关系表")
    private List<TableDefinition> relationTables;

    public static RelationCrossUpQo of() {
        return new RelationCrossUpQo();
    }
}
