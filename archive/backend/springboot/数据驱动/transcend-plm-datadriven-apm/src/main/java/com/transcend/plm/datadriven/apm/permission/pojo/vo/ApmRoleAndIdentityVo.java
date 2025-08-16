package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Describe
 * @author yuhao.qiu
 * @Date 2023/10/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmRoleAndIdentityVo {

    private ApmRoleVO apmRoleVO;

    private List<ApmUser> apmUserList;
}
