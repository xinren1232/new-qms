package com.transcend.plm.configcenter.api.model.view.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Set;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 15:43
 **/
@Data
@ApiModel("标准属性")
@Accessors(chain = true)
public class CfgViewQo {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属bid
     */
    @ApiModelProperty(value = "所属bid", example = "123456", required = true)
    private String belongBid;

    /**
     * 视图作用域
     */
    private String viewScope;

    /**
     * 编码
     */
    @ApiModelProperty(value = "类型", example = "country", required = true)
    private String type;

    /**
     * 编码
     */
    @ApiModelProperty(value = "类型集合", example = "types", required = true)
    private Set<String> types;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称", example = "项目视图", required = true)
    private String name;

    /**
     * 对象code
     */
    @ApiModelProperty(value = "对象code", example = "xxx", required = false)
    private String modelCode;

    /**
     * 对象code 继承条件查询
     */
    @ApiModelProperty(value = "对象code 继承条件查询", example = "xxx", required = false)
    private String modelCodeInherit;

    /**
     * tag
     */
    @ApiModelProperty(value = "tag", example = "tag", required = true)
    private String tag;

    /**
     * 优先级
     */
    @ApiModelProperty(value = "优先级", example = "1", required = false)
    private Integer priority;

    @ApiModelProperty(value = "视图类型", example = "1", required = true)
    private String viewType;

    /**
     * 使用状态，禁用2，启用1，未启用0
     */
    @ApiModelProperty(value = "使用状态，禁用2，启用1，未启用0", example = "0", required = false)
    private Integer enableFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static CfgViewQo of() {
        return new CfgViewQo();
    }
}
