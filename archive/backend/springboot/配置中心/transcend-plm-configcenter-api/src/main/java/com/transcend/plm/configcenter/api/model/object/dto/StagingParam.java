package com.transcend.plm.configcenter.api.model.object.dto;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 对象暂存参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:41
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象暂存参数")
public class StagingParam implements Serializable {

    public static StagingParam of() {
        return new StagingParam();
    }

    @ApiModelProperty(value = "业务ID(编码)")
    private String bid;

    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "版本")
    private Integer version;

    @ApiModelProperty(value = "modelCode+version的唯一id")
    private String modelVersionCode;

    @ApiModelProperty(value = "父的modelCode+version的唯一id")
    private String parentModelVersionCode;

    @ApiModelProperty(value = "父对象版本")
    private Integer parentVersion;

    @ApiModelProperty(value = "业务编码（对应SAP的编码）")
    private String code;

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "图标(存放路径)")
    private String avatar;

    @ApiModelProperty(value = "使用状态，禁用disable，启用enable，未启用off(默认off)")
    private Integer enableFlag;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "根模型")
    private String baseModel;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "是否有草稿")
    private Boolean draft;

    @ApiModelProperty(value = "是否关系对象")
    private Byte relationFlag;

    @ApiModelProperty(value = "是否有版本")
    private Byte versionFlag;

    @ApiModelProperty(value = "属性")
    private List<CfgObjectAttributeVo> attrList;

    private static final long serialVersionUID = 1L;
}
