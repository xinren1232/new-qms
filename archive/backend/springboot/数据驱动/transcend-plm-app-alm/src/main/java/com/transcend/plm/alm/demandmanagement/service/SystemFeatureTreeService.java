package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.alm.demandmanagement.entity.ao.SfTreeDataSyncCopyAo;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;

import java.util.List;

/**
 * 特性树服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/14 09:34
 */
public interface SystemFeatureTreeService {

    /**
     * 同步数据
     *
     * @param ao 参数
     */
    void syncData(SfTreeDataSyncCopyAo ao);


    /**
     * 获取系统特性树
     *
     * @param searchKey 搜索关键字
     * @return 特性树列表
     */
    List<AlmSystemFeatureDTO> getSystemFeatureTree(String searchKey);

    /**
     * 通过特性bid获取特性信息
     *
     * @param bid 特性bid
     * @return 特性信息
     */
    AlmSystemFeatureDTO getSystemFeatureByBid(String bid);

}
