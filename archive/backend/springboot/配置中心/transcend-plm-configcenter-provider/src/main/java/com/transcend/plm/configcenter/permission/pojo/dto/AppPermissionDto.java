package com.transcend.plm.configcenter.permission.pojo.dto;

import com.transcend.plm.configcenter.permission.pojo.vo.PermissionOperationItemVo;
import lombok.Data;

import java.util.List;

@Data
public class AppPermissionDto {
    /**
     * 对象编码
     */
    private String modelCode;


    /**
     * 应用基础权限BID
     */
    private String permissionBid;

    private List<PermissionOperationItemVo> appPermissionOperationList;

  /**
     * 应用基础操作列表
     */
    List<PermissionOperationItemDto> operationItems;

    private String roleCode;

    private List<String> operatorCodeList;
}
