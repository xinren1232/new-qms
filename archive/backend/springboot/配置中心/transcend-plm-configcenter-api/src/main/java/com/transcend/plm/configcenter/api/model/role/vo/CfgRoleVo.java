package com.transcend.plm.configcenter.api.model.role.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgRoleVo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;


    /**
     * 父编码
     */
    private String parentBid;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子角色
     */
    private List<? extends CfgRoleVo> children;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;


    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}
