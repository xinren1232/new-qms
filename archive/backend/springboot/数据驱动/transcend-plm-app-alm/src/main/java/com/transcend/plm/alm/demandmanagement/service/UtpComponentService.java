package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.alm.demandmanagement.entity.ao.UtpComponentTreeListAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.UtpComponentTreeVo;

import java.util.List;

/**
 * utp模块服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 16:25
 */
public interface UtpComponentService {

    /**
     * 数据全量修正
     */
    void dataCorrection();

    /**
     * 获取树列表
     *
     * @param ao 请求参数
     * @return 树列表
     */
    List<UtpComponentTreeVo> getComponentTreeList(UtpComponentTreeListAo ao);

}
