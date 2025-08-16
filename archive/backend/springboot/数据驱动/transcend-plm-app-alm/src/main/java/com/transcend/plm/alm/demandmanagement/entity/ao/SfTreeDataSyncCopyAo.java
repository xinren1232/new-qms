package com.transcend.plm.alm.demandmanagement.entity.ao;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 特性树形对象复制同步参数
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/12 15:07
 */
@Data
public class SfTreeDataSyncCopyAo implements Serializable {

    /**
     * 关系模型编码
     */
    private String relationModelCode;
    /**
     * 同步关联源Bid
     */
    private String sourceBid;
    /**
     * 同步特性数据Bid列表
     */
    private List<String> bidList;
    /**
     * 空间Bid
     */
    private String spaceBid;
    /**
     * 空间应用Bid
     */
    private String spaceAppBid;


}
