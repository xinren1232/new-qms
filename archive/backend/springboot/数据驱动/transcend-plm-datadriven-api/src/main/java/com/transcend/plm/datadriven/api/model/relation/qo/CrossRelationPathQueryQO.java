package com.transcend.plm.datadriven.api.model.relation.qo;

import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jie.yan
 * @date 2024/3/22
 */
@Data
public class CrossRelationPathQueryQO {


    /**
     *  方位 up、down
     */
    @ApiModelProperty(value = "方位 up、down")
    private String direction;

    @ApiModelProperty("关系表")
    private TableDefinition relationTable;

    @ApiModelProperty("实例表")
    private TableDefinition instanceTable;

    /**
     *  前一关系方位 up、down
     */
    @ApiModelProperty(value = "上一条数据方位  up、down")
    private String preDirection;

    /**
     * 查询条件
     */
    @ApiModelProperty(value = "查询条件")
    private List<QueryWrapper> queries;
}
