package com.transcend.plm.datadriven.api.model.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 对象模型VO
 *
 * @author jingfang.Luo
 * @date 2022/11/24 11:43
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象模型VO")
public class ObjectVo {

    /**
     * @return {@link ObjectVo }
     */
    public static ObjectVo of() {
        return new ObjectVo();
    }


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

    @ApiModelProperty(value = "是否是多对象")
    private Byte  isMultiObjectModel;

    @ApiModelProperty(value = "锁信息：提示哪个对象被谁锁了")
    private String lockInfo;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

    @ApiModelProperty(value = "多语言字典")
    private Map<String, String> langDict;

    @ApiModelProperty(value = "表bid")
    private String tableBid;

    @ApiModelProperty(value = "属性")
    private List<ObjectAttributeVo> attributes;

    private static final long serialVersionUID = 1L;
}
