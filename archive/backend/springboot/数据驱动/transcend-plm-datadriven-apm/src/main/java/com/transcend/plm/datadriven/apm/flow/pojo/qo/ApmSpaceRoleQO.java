package com.transcend.plm.datadriven.apm.flow.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Describe 空间配置 - 角色&权限查询对象
 * @Author yuhao.qiu
 * @Date 2023/10/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmSpaceRoleQO {

    @ApiModelProperty(value = "空间bid、SpaceAppbid", required = true)
    @NotBlank(message = "bid不能为空")
    private String bid;

    @ApiModelProperty(value = "域类型", required = true)
    private String sphereType;

    @ApiModelProperty(value = "角色类型", required = true)
    private String type;
}
