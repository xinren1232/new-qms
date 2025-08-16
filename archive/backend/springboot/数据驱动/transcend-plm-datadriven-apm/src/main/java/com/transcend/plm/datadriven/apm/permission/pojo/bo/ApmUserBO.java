package com.transcend.plm.datadriven.apm.permission.pojo.bo;

import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 用户BO
 * @createTime 2023-09-21 14:56:00
 */
@Data
public class ApmUserBO implements ApmIdentity {

    public ApmUserBO() {
    }

    public ApmUserBO(ApmUser apmUser) {
        this.empNo = apmUser.getEmpNo();
        this.name = apmUser.getName();
        this.enName = apmUser.getEnName();
        this.age = apmUser.getAge();
        this.departmentList = apmUser.getDepartmentList();
        this.gender = apmUser.getGender();
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
     * 部门列表
     */
    private List<String> departmentList;
    /**
     * 性别
     */
    private String gender;

    @Override
    public List<ApmUser> getApmUserList() {
        return Lists.newArrayList(new ApmUser(this));
    }
}
