package com.transcend.plm.datadriven.api.model.relation.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 关系路径链实体类
 *
 * @author jie.yan
 * @date 2024/3/21
 */


@Data
public class CrossRelationPathChainQO {

    /**
     * 当前的源对象实例bid
     */
    @ApiModelProperty("当前的源对象实例bid")
    private String currentSourceBid;

    /**
     * 当前的源对象实例bid
     */
    @ApiModelProperty("当前的源对象modelCode")
    private String currentSourceModelCode;

    /**
     * 当前空间Bid
     */
    @ApiModelProperty("当前空间应用Bid")
    private String spaceAppBid;

    /**
     * 当前空间Bid
     */
    @ApiModelProperty("当前空间Bid")
    private String spaceBid;

    /**
     * 关系路径链
     */
    @ApiModelProperty(value = "关系路径链")
    @NotEmpty(message = "关系路径链为空！")
    private List<CrossRelationPathQO> paths;


    @ApiModelProperty("标准查询条件")
    private BaseRequest<ModelMixQo> pageQo;
}
