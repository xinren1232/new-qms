package com.transcend.plm.datadriven.apm.flow.pojo.qo;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Describe Apm启动流程QO
 * @Author yuhao.qiu
 * @Date 2024/3/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmFlowQo {

    /**
     * 空间Bid
     */
    private String spaceBid;

    /**
     * 空间应用Bid
     */
    private String spaceAppBid;

    /**
     * 角色列表
     */
    List<ApmRoleUserAO> roleUserList;
}
