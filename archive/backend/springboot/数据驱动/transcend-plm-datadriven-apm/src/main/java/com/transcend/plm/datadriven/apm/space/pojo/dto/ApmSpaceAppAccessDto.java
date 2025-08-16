package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Describe 空间对象权限dto
 * @Author yuhao.qiu
 * @Date 2023/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmSpaceAppAccessDto {

    @ApiModelProperty(value = "对象SpaceAppId")
    private String spaceAppId;

    @ApiModelProperty(value = "对象可操作的权限")
    private ApmAccess objectAccess;
}
