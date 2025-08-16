package com.transcend.plm.alm.pi.dto;

import lombok.Data;

@Data
public class MemberDto {
    /**姓名*/
    private String name;
    /**产品表关联Id*/
    private String productId;
    /**1=产品负责人 2=技术负责人 3=产品经理 4=开发人员 5=技术架构管理 6=QA*/
    private String type;
    /**用户Id*/
    private String userId;
    /**工号*/
    private String employeeNo;
}
