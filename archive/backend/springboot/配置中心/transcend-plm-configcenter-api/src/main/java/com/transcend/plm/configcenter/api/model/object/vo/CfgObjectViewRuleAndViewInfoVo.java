package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 对象视图参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 11:36
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象视图信息")
public class CfgObjectViewRuleAndViewInfoVo extends CfgObjectViewRuleVo {

    public static CfgObjectViewRuleAndViewInfoVo of() {
        return new CfgObjectViewRuleAndViewInfoVo();
    }

    @ApiModelProperty(value = "视图名称", example = "00000001")
    private String viewName;
}
