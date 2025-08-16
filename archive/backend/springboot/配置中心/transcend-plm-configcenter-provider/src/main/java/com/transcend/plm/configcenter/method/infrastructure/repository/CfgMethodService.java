package com.transcend.plm.configcenter.method.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgMethod;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:00
 **/
public interface CfgMethodService extends IService<CfgMethod> {

    /**
     * 修改
     * @param cfgMethod
     * @return
     */
    boolean updateByBid(CfgMethod cfgMethod);

    /**
     * 根据bid获取数据
     * @param bid
     * @return
     */
    CfgMethod getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgMethodVo> pageByCfgMethodQo(BaseRequest<CfgMethodQo> pageQo);

    /**
     * 根据bid删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);
}
