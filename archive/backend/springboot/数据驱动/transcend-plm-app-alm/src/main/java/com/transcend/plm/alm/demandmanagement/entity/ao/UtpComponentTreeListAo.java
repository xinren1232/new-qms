package com.transcend.plm.alm.demandmanagement.entity.ao;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * utp模块查询参数
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 16:28
 */
@Data
public class UtpComponentTreeListAo implements Serializable {

    /**
     * 模块/子模块负责人工号
     */
    @ApiModelProperty("模块/子模块负责人工号")
    private List<String> ownerUserList;

    /**
     * 关键词搜索
     */
    @ApiModelProperty("关键词搜索")
    private String searchKey;

}
