package com.transcend.plm.configcenter.filemanagement.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CfgFileLibraryRuleRelDto {

    private String bid;

    /**
     * 规则bid
     */
    private String ruleBid;

    /**
     * 源文件库bid
     */
    private String sourceLibraryBid;

    /**
     * 目标文件库bids
     */
    private List<String> targetLibraryBids;
}
