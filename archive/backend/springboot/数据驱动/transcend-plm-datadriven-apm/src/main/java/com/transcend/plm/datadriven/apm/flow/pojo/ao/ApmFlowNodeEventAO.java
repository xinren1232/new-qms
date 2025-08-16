package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeEventAO {
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
     * 对象字段值类型，now.当前时间，loginUser.当前登录用户
     */
    private String filedValueType;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 指定字段值
     */
    private String filedValue;

    /**
     * 指定字段值
     */
    private String filedOtherValue;

    /**
     * 关联驱动列表
     */
    private List<ApmFlowDriveRelateAO> driveRelateList;

}
