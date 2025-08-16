package com.transcend.plm.datadriven.apm.flow.pojo.qo;

import lombok.Data;

/**
 * 复制角色和成员Qo
 * @author yinbin
 * @version:
 * @date 2023/12/07 10:45
 */
@Data
public class ApmCopyRoleAndIdentityQo {

    /**
     * 当前空间、应用、实例bid
     */
    private String targetBid;

    /**
     * 所选空间、应用、实例bid
     */
    private String sourceBid;

    /**
     * 类型(空间space、应用object、实例instance)
     */
    private String type;
}
