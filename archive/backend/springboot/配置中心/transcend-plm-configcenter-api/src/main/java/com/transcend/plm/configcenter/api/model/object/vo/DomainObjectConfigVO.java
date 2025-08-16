package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 域对象配置VO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/30 10:48
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class DomainObjectConfigVO extends BaseDto{

    public static DomainObjectConfigVO of() {
        return new DomainObjectConfigVO();
    }


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
    private String description;

    private static final long serialVersionUID = 1L;
}
