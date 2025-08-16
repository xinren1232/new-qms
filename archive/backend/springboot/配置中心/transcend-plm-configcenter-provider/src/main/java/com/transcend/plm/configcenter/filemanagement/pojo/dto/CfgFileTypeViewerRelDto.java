package com.transcend.plm.configcenter.filemanagement.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CfgFileTypeViewerRelDto {
    /**
     * 文件类型bid
     */
    private String fileTypeBid;
    /**
     * 文件查看器bid列表
     */

    private List<String> fileViewerBidList;
    /**
     * 操作类型 add, delete
     */

    private String operateType;
}
