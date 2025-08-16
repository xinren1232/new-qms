package com.transcend.plm.configcenter.method.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.method.infrastructure.repository.CfgEventService;
import com.transcend.plm.configcenter.method.infrastructure.repository.mapper.CfgEventMapper;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgEvent;
import com.transcend.plm.configcenter.method.pojo.CfgEventConverter;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:00
 **/
@Service
public class CfgEventServiceImpl extends ServiceImpl<CfgEventMapper, CfgEvent>
implements CfgEventService {
    @Override
    public boolean updateByBid(CfgEvent cfgEvent) {
        Assert.hasText(cfgEvent.getBid(),"bid is blank");
        LambdaUpdateWrapper<CfgEvent> condition = Wrappers.<CfgEvent>lambdaUpdate()
                .eq(CfgEvent::getBid, cfgEvent.getBid());
        return this.update(cfgEvent, condition);
    }

    /**
     * 根据bid获取数据
     *
     * @param bid
     * @return
     */
    @Override
    public CfgEvent getByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.getOne(Wrappers.<CfgEvent>lambdaQuery().eq(CfgEvent::getBid, bid));
    }

    /**
     * 分页查询
     *
     * @param pageQo
     * @return
     */
    @Override
    public PagedResult<CfgEventVo> pageByCfgEventQo(BaseRequest<CfgEventQo> pageQo) {
        CfgEvent cfgEvent = CfgEventConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgEvent> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        IPage<CfgEvent> iPage= this.page(page, Wrappers.query(cfgEvent));
        List<CfgEventVo> cfgEventVos = CfgEventConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgEventVos);
    }

    /**
     * 根据bid删除
     *
     * @param bid
     * @return
     */
    @Override
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText("bid","bid is blank");
        return this.remove(Wrappers.<CfgEvent>lambdaQuery().eq(CfgEvent::getBid, bid));
    }
}
