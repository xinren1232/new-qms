package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeEventVO {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 节点业务bid
     */
    private String nodeBid;

    /**
     * 事件分类，1.节点到达事件，2.节点完成事件
     */
    private Integer eventClassification;

    /**
     * 事件类型，1.状态扭转，2.修改字段值，3.指定节点负责人，4.指定角色负责人,5.自定义方法
     */
    private Integer eventType;

    /**
     * 自定义方法路径
     */
    private String nodeMethodPath;

    /**
     * 对象字段名称
     */
    private String filedName;

    /**
     * 对象字段类型，string.字符串，number.数字，date.日期，role.角色
     */
    private String filedType;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 指定字段值
     */
    private String filedValue;

    /**
     * 对象字段值类型，now.当前时间，loginUser.当前登录用户
     */
    private String filedValueType;

    /**
     * 指定字段值
     */
    private String filedOtherValue;

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
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;
}
