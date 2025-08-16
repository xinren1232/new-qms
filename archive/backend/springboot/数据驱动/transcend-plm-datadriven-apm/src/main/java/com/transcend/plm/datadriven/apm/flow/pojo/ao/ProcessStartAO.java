package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程启动AO
 * @createTime 2023-10-07 13:58:00
 */
@Data
public class ProcessStartAO {
    private String modelCode;
    private String instanceBid;
    private List<ApmRoleUserAO> roleUserList;
}
