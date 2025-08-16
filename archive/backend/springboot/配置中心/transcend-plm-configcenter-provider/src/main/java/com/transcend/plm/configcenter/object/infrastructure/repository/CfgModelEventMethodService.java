package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:00
 **/
public interface CfgModelEventMethodService extends IService<CfgModelEventMethodPo> {

    /**
     * 修改
     * @param cfgModelEventMethod
     * @return
     */
    boolean updateByBid(CfgModelEventMethodPo cfgModelEventMethod);

    /**
     * 根据bid获取数据
     * @param bid
     * @return
     */
    CfgModelEventMethodPo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgModelEventMethodVo> pageByCfgModelEventMethodQo(BaseRequest<CfgModelEventMethodQo> pageQo);

    /**
     * 根据bid删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);

    /**
     * 获取列表
     * @param cfgModelEventMethod
     * @return
     */
    List<CfgModelEventMethodPo> findByCondition(CfgModelEventMethodPo cfgModelEventMethod);

    /**
     * 批量新增
     * @param dtos
     * @return
     */
    List<CfgModelEventMethodPo> bulkAdd(List<CfgModelEventMethodPo> dtos);

    /**
     * 批量删除
     * @param bids
     * @return
     */
    Boolean bulkDelete(List<String> bids);
}
