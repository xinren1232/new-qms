package com.transcend.plm.configcenter.api.model.object.qo;

import com.transcend.plm.configcenter.api.model.object.dto.CfgPropertyMatchDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author 权限-对象
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public class CfgObjectViewRuleQo implements Serializable {

    /**
     * 对象BID
     */
    @ApiModelProperty(value = "对象BID", example = "232323")
    private String objBid;

    /**
     * 对象类型
     */
    @ApiModelProperty(value = "对象BID", example = "232323")
    private String baseModel;

    /**
     * 对象类型
     */
    @ApiModelProperty(value = "对象BID", example = "232323")
    private List<String> baseModels;

    /**
     * 对象BID 集合
     */
    @ApiModelProperty(value = "对象BID 集合", example = "['324324']")
    private List<String> objBids;

    /**
     * 角色类型（1-系统，0-业务）
     */
    private Byte roleType;

    /**
     * 模糊查询like的一个按钮
     */
    private String likeOperation;

    /**
     * 模糊查询like的一个模型实例
     */
    private String likeModelCode;

    /**
     * 字段匹配参数
     */
    @ApiModelProperty(value = "字段匹配", example = "doing")
    private CfgPropertyMatchDto propertyMatchParams;

    /**
     * 角色BID 集合
     */
    @ApiModelProperty(value = "角色Code 集合", example = "['324324']")
    private List<String> roleCodes;

    /**
     * 对象父BID
     */
    private String objParentBid;

    @ApiModelProperty(value = "生命周期", example = "published")
    private String lifeCycleCode;

    public static CfgObjectViewRuleQo of() {
        return new CfgObjectViewRuleQo();
    }

    private static final long serialVersionUID = 1L;
}
