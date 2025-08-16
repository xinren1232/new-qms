package com.transcend.plm.datadriven.api.model.qo;

import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.Order;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 模型混合查询对象
 *
 * @author yss
 * @version: 1.0
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "领域对象属性QO",tags = "操作领域对象QO")
public class ModelMixQo {


    /**
     * 查询条件
     */
    @ApiModelProperty(value = "通用查询（目前支持 名称模糊+编码全匹配）")
    private String generalSearch;


    /**
     * 查询条件
     */
    @ApiModelProperty(value = "查询条件")
    private List<ModelFilterQo> queries;

    /**
     * 是否任意匹配全部
     */
    @ApiModelProperty(value = "是否任意匹配全部")
    private Boolean anyMatch;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private List<Order> orders;

    @ApiModelProperty(value = "树")
    List<MObjectTree> tree;
    @ApiModelProperty(value = "分组字段")
    private String groupProperty;
    @ApiModelProperty(value = "分组字段值")
    private String groupPropertyValue;

    /**
     * 是否导航查询
     */
    private boolean isNavQuery = false;
    /**
     * 导航视图bid
     */
    private String appViewConfigBid;

    /**
     * 关系modelCode
     */
    private String relationModelCode;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页号
     */
    private Integer pageCurrent;

    public static ModelMixQo of() {
        return new ModelMixQo();
    }





}
