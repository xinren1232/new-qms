package com.transcend.plm.configcenter.api.model.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-23 09:53
 **/
@Data
public class BaseDto implements Serializable {

    private Long id;

    /**
     * 业务ID(编码)
     */
    @ApiModelProperty(value="业务ID(编码)")
    private String bid;

    @ApiModelProperty(value = "未启用0，启用1，禁用2")
    private Integer enableFlag;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "删除标识：未删0，删除1")
    private Integer deleteFlag;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

}
