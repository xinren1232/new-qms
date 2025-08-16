package com.transcend.plm.datadriven.api.model.relation.qo;

import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jie.yan
 * @date 2024/3/22
 */

@Data
@Accessors(chain = true)
public class CrossRelationPathChainQueryQO {


    @ApiModelProperty("当前的源对象实例bid")
    private String currentSourceBid;

    @ApiModelProperty("目的地源对象表定义")
    private TableDefinition sourceTable;

    @ApiModelProperty(value = "查询条件")
    private List<QueryWrapper> sourceQueries;

    @ApiModelProperty(value = "方位")
    private String direction;

    @ApiModelProperty(value = "空间Bid")
    private String spaceBid;

    @ApiModelProperty("跨层级路径查询条件")
    private List<CrossRelationPathQueryQO> pathQueries;

    /**
     * @return {@link CrossRelationPathChainQueryQO }
     */
    public static CrossRelationPathChainQueryQO of() {
        return new CrossRelationPathChainQueryQO();
    }
}
