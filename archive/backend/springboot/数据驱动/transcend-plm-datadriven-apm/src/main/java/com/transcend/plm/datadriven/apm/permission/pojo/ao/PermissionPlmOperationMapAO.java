package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author quan.cheng
 * @title PermissionPlmOperationMapAO
 * @date 2024/5/7 15:40
 * @description 操作权限映射实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PermissionPlmOperationMapAO {

    private Integer id;

    /**
     * 业务id，
     */
    private String bid;

    /**
     * 操作code,ADD/EDIT...
     */
    private String operationCode;

    /**
     * 操作名称
     */
    private String operationName;

    /**
     * 访问操作key,多个对应acess_plm_rule_item表的operation_add
     */
    private String accessOperationKey;

    /**
     * 应用id，
     */
    private String appBid;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;


}
