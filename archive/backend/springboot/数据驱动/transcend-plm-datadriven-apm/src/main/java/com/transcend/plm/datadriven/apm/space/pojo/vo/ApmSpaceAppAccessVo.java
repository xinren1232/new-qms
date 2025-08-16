package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmAccess;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Describe 空间对象权限dto
 * @Author yuhao.qiu
 * @Date 2023/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmSpaceAppAccessVo {

    @ApiModelProperty(value = "对象SpaceAppBid")
    private String spaceAppBid;

    @ApiModelProperty(value = "对象可操作的权限列表")
    private List<ApmAccess> objectAccessList;
}
