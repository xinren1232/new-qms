package com.transcend.plm.datadriven.apm.feign.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/15 10:29
 * @Describe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PlatFormUserDTO {
    private String uid;
    private String employeeName;
    private String enName;
    private String employeeNickName;
    private String employeeNo;
    private String deptId;
    private String deptNo;
    private String deptName;
}
