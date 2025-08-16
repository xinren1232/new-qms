package com.transcend.plm.configcenter.role.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CfgRoleDto implements Serializable {

    /**
     * 业务id
     */
    private String bid;


    /**
     * 父编码
     */
    private String parentBid;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;


    /**
     * 描述说明
     */
    private String description;

    /**
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}
