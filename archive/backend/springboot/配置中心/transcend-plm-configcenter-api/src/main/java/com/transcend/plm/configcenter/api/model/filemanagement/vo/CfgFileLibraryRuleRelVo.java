package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

import java.util.List;

@Data
public class CfgFileLibraryRuleRelVo implements java.io.Serializable {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 规则业务id
     */
    private String ruleBid;

    /**
     * 源文件库业务id
     */
    private String sourceLibraryBid;

    private CfgFileLibraryVo sourceLibrary;

    /**
     * 目标文件库业务id
     */
    private List<String> targetLibraryBids;

    private List<CfgFileLibraryVo> targetFileLibraryList;


}
