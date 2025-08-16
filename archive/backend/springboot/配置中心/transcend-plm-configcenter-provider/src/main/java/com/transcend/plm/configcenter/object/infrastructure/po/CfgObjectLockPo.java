package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * ObjectLockPO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 11:31
 */
@Getter
@Setter
@ToString
@TableName("cfg_object_lock")
public class CfgObjectLockPo implements Serializable {

    @ApiModelProperty(value = "主键ID")
    private Integer id;

    @ApiModelProperty(value = "对象bid")
    private String objBid;

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "检出对象名称")
    private String objectName;

    @ApiModelProperty(value = "检出人工号")
    private String checkoutBy;

    @ApiModelProperty(value = "检出人名称")
    private String checkoutName;

    private static final long serialVersionUID = 1L;
}
