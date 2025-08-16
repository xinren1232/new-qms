package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmRoleAccessAO {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 资源bid
     */
    private String accessBid;

    /**
     * 域bid
     */
    private String sphereBid;

    /**
     * 对象编码
     */
    private String spaceAppBid;

    /**
     * 对象字段名称
     */
    private String filedName;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 关系
     */
    private String relationship;

    /**
     * 条件比较值
     */
    private String filedValue;
}
