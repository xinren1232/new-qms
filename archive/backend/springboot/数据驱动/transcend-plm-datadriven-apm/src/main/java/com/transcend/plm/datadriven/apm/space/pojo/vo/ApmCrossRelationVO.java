package com.transcend.plm.datadriven.apm.space.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 跨层级关系VO
 * @createTime 2023-11-04 15:18:00
 */
@Data
@Accessors(chain = true)
public class ApmCrossRelationVO implements java.io.Serializable {

    /**
     * 编码
     */
    @ApiModelProperty(value = "编码")
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 信息
     */
    @ApiModelProperty(value = "信息")
    private Object infos;

    public static ApmCrossRelationVO of() {
        return new ApmCrossRelationVO();
    }
}
