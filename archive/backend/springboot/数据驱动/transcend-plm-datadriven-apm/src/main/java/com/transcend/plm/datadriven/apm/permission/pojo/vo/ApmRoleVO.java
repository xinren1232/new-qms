package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色VO
 * @createTime 2023-09-20 15:01:00
 */
@Data
public class ApmRoleVO {
    private Integer id;

    /**
     *
     */
    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 层级路径
     */
    private String path;

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
     * 父级bid
     */
    private String parentBid;

    /**
     * 角色来源(自定义/系统内置)
     */
    private String roleOrigin;

    /**
     * 域id
     */
    private String sphereBid;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 外部唯一标识
     */
    private String foreignBid;

    /**
     * 是否是内置角色角色
     */
    private boolean innerRole;

    /**
     * 子角色
     */
    private List<ApmRoleVO> childRoles;

    /**
     * 成员
     */
    private List<ApmRoleIdentityVO> members;
}
