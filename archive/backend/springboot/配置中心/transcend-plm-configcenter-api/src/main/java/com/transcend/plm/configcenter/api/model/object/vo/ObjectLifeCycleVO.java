package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 对象生命周期状态DTO
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2021/02/01 14:53
 */
@Getter
@Setter
@ApiModel(value = "对象生命周期实体类",description = "对象生命周期实体类")
@Accessors(chain = true)
@ToString
public class ObjectLifeCycleVO extends BaseDto{



    @ApiModelProperty(value = "生命周期模板id",example = "1")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期模板名称",example = "测试")
    private String lcTemplName;

    @ApiModelProperty(value = "生命周期状态版本",example = "V1.0")
    private String lcTemplVersion;

    @ApiModelProperty(value = "生命周期初始状态",example = "plan")
    private String initState;

    @ApiModelProperty(value = "生命周期初始状态中文",example = "筹划中")
    private String initStateName;

    @ApiModelProperty(value = "说明",example = "说明...")
    private String description;

    @ApiModelProperty(value = "关联的对象的业务ID",example = "1")
    private String objBid;

    @ApiModelProperty(value = "关联的对象的版本",example = "V1.0")
    private String objVersion;

    @ApiModelProperty(value = "是否自定义生命周期模板",example = "false")
    private Boolean custom;

    public static ObjectLifeCycleVO of() {
        return  new ObjectLifeCycleVO();
    }

    @ApiModelProperty(value = "是否是父类继承",example = "1 是，0 否")
    private Boolean extend;

}
