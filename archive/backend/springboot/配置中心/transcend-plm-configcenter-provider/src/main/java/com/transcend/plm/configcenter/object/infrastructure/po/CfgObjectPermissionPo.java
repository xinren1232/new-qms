package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * @author
 * 权限-对象表
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
@TableName("cfg_object_permission")
public class CfgObjectPermissionPo extends BasePoEntity {


    /**
     * 对象业务id
     */
    private String objBid;

    /**
     * 对象类型（project-项目，task-任务，doc-文档）
     */
    private String baseModel;

    /**
     * 角色类型（1-系统，0-业务）
     */
    private Byte roleType;

    /**
     * 角色表bid
     */
    private String roleCode;

    /**
     * 生命周期状态CODE(还有 ALL)
     */
    private String lcStateCode;

    /**
     * 操作权限
     */
    @TableField(typeHandler = com.transcend.plm.configcenter.common.tools.CommaTypeHandler.class)
    private Set<String> operations;

    /**
     * 模型code
     */
    private String modelCode;

    private static final long serialVersionUID = 1L;

    public static CfgObjectPermissionPo of(){
        return new CfgObjectPermissionPo();
    }
}
