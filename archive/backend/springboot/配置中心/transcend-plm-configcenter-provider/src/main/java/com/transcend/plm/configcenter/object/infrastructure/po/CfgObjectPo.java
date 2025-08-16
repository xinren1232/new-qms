package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * ObjectModelPO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 15:59
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cfg_object")
public class CfgObjectPo extends BasePoEntity{

    public static CfgObjectPo of() {
        return new CfgObjectPo();
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

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "对象名称")
    private String tableBid;

    @ApiModelProperty(value = "图标(存放路径)")
    private String avatar;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "类型")
    private String type;

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

    @ApiModelProperty(value = "多语言字典")
    private Map<String, String> langDict;

    @ApiModelProperty(value = "是否树形结构")
    private Boolean treeFlag;

    private static final long serialVersionUID = 1L;
}
