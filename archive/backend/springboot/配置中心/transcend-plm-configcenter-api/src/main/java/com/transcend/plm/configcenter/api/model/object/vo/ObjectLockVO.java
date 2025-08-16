package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 对象锁VO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/06 15:19
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象锁VO")
public class ObjectLockVO implements Serializable {

    public static ObjectLockVO of() {
        return new ObjectLockVO();
    }

    @ApiModelProperty(value = "锁信息")
    private String lockInfo;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

}
