package com.transcend.plm.configcenter.view.application.service;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.configcenter.view.pojo.qo.SyncViewContentQo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:34
 **/
public interface ICfgViewApplicationService {
    /**
     * 保存或新增基础属性
     *
     * @param cfgViewDto
     * @return
     */
    CfgViewVo saveOrUpdate(CfgViewDto cfgViewDto);

    /**
     * 保存或新增基础属性(判断bid是否存在，存在则更新，不存在则新增
     *
     * @param bid
     * @param cfgViewDto
     * @return
     */
    CfgViewVo saveOrUpdate(String bid, CfgViewDto cfgViewDto);

    /**
     * 拷贝应用视图数据，map key->oldBid,value->newBid
     *
     * @param cfgViewDto
     * @return
     */
    <T> boolean copyViews(CfgViewDto cfgViewDto);

    /**
     * 局部更新内容
     *
     * @param bid
     * @param partialContent 局部内容
     * @return Boolean
     */
    Boolean updatePartialContent(String bid, Map<String, Object> partialContent);

    /**
     * 获取视图数据
     *
     * @param param 查询条件
     * @return CfgViewVo
     */
    CfgViewVo getView(ViewQueryParam param);

    /**
     * 批量获取视图数据
     *
     * @param param 查询条件
     * @return Map<String, CfgViewVo>
     */
    Map<String, CfgViewVo> getViews(ViewQueryParam param);

    /**
     * 通过belongBid获取视图列表
     *
     * @param belongBid belongBid
     * @return List<CfgViewVo>
     */
    List<CfgViewVo> listView(String belongBid);

    /**
     * 通过belongBid获取视图列表
     *
     * @param belongBids belongBid
     * @return List<CfgViewVo>
     */
    List<CfgViewVo> listView(List<String> belongBids);

    /**
     * 根据参数获取视图元模型
     *
     * @param param 查询参数
     * @return List<CfgViewMetaDto>
     */
    List<CfgViewMetaDto> getMetaModelsByParam(ViewQueryParam param);

    /**
     * 获取视图类型或者默认视图
     *
     * @param param 查询参数
     * @return List<CfgViewVo>
     */
    List<CfgViewVo> listTypeOrDefaultView(ViewQueryParam param);

    /**
     * 同步视图配置
     *
     * @param qo 同步视图配置参数
     */
    void syncViewContent(SyncViewContentQo qo);

    /**
     * 获取视图上一次同步的视图配置
     *
     * @param viewBid 视图bid
     * @return List<String>
     */
    List<String> lastSyncConfig(String viewBid);
}
