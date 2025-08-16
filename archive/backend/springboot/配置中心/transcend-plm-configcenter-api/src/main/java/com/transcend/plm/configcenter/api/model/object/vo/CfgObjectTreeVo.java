package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 对象模型树
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:42
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象模型树")
public class CfgObjectTreeVo implements Serializable {

    public static CfgObjectTreeVo of() {
        return new CfgObjectTreeVo();
    }

    @ApiModelProperty(value = "业务ID(编码)")
    private String bid;

    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "根模型")
    private String baseModel;

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "使用状态，禁用disable，启用enable，未启用off(默认off)")
    private Integer enableFlag;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "是否有草稿")
    private Boolean draft;

    @ApiModelProperty(value = "锁信息：提示哪个对象被谁锁了")
    private String lockInfo;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

    @ApiModelProperty(value = "对象类型")
    private String type;

    @ApiModelProperty(value = "子对象")
    private List<CfgObjectTreeVo> children;

    private static final long serialVersionUID = 1L;

}
