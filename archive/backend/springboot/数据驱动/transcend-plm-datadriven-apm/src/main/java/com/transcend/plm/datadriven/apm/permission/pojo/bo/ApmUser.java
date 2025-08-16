package com.transcend.plm.datadriven.apm.permission.pojo.bo;

import com.transsion.framework.uac.model.dto.DepartmentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023-10-09 10:31:00
 */
@Data
@Builder
@AllArgsConstructor
public class ApmUser{
    public ApmUser() {
    }

    public ApmUser(ApmUserBO apmUserBO) {
        this.empNo = apmUserBO.getEmpNo();
        this.name = apmUserBO.getName();
        this.enName = apmUserBO.getEnName();
        this.age = apmUserBO.getAge();
        this.departmentList = apmUserBO.getDepartmentList();
        this.gender = apmUserBO.getGender();
    }
    /**
     * 工号
     */
    private String empNo;
    /**
     * 姓名
     */
    private String name;

    private String enName;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 部门Id列表
     */
    private List<String> departmentList;

    /**
     * 部门列表
     */
    private List<DepartmentDTO> depts;

    /**
     * 性别
     */
    private String gender;

}
