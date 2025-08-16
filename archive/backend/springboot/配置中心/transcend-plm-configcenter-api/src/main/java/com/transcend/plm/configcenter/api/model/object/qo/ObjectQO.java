package com.transcend.plm.configcenter.api.model.object.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * ObjectQO
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/19 10:57
 */
@Setter
@Getter
@Accessors(chain = true)
public class ObjectQO {


    /**对象bid ,必填*/
    private String bid;

    @ApiModelProperty(value = "对象业务对应Sap编码(新增必填)", example = "0000123")
    private String code;

    /**对象版本 不填，查最新*/
    private String version;

    /**是否需要查询属性*/
    private Boolean queryAttr;

    /**是否需要查询生命周期*/
    private Boolean queryLifeCycle;

    /**是否需要查询权限*/
    private Boolean queryAuth;

    /**是否需要查询事件*/
    private Boolean queryEvent;

    /**是否需要查询流程*/
    private Boolean queryFlow;


    public static ObjectQO of(){
        return new ObjectQO();
    }

}
