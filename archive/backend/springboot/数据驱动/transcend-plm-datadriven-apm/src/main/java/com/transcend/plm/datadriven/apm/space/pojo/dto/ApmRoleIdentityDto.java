package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author yanjie
 * @Date 2023/12/27 14:08
 * @Version 1.0
 */


@Data
public class ApmRoleIdentityDto {

    private Integer id;

    private String foreignBid;

    private List<String> roleBids;
}
