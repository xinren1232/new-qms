package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TODO 描述
 *
 * @author leigang.yang
 * @version: 1.0
 * @date 2022/12/30 17:27
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "新增域对象参数")
public class DomainObjectConfigFindParam implements Serializable {

    public static DomainObjectConfigFindParam of() {
        return new DomainObjectConfigFindParam();
    }

    @ApiModelProperty(value = "业务id")
    private String bid;

    @ApiModelProperty(value = "项目或者存储域bid")
    private String domainBid;

    @ApiModelProperty(value = "对象id")
    private String objBid;

    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "对象类型（任务，文档等等）")
    private String type;

    @ApiModelProperty(value = "对象类型（任务，文档等等）")
    private String baseModel;

    @ApiModelProperty(value = "生命周期模板id")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期模板版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "模板id")
    private String templBid;

    @ApiModelProperty(value = "生命周期初始化状态")
    private String initState;

    @ApiModelProperty(value = "备注")
    private String note;

    private static final long serialVersionUID = 1L;
}
