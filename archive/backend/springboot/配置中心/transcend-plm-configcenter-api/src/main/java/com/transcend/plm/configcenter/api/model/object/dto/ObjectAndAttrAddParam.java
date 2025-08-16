package com.transcend.plm.configcenter.api.model.object.dto;

//import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
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
 * @author yinbin
 * @version:
 * @date 2023/01/09 09:25
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "新增对象和属性参数")
public class ObjectAndAttrAddParam implements Serializable {
    private static final long serialVersionUID = 1L;

    public static ObjectAndAttrAddParam of() {
        return new ObjectAndAttrAddParam();
    }

    @ApiModelProperty(value = "对象基本信息")
    private ObjectAddParam objectAddParam;

    @ApiModelProperty(value = "属性")
    private List<CfgObjectAttributeVo> attrList;

}
