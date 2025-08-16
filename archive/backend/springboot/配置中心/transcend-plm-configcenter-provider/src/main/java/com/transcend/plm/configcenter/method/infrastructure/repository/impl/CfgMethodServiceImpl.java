package com.transcend.plm.configcenter.method.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.method.infrastructure.repository.CfgMethodService;
import com.transcend.plm.configcenter.method.infrastructure.repository.mapper.CfgMethodMapper;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgMethod;
import com.transcend.plm.configcenter.method.pojo.CfgMethodConverter;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
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
public class CfgMethodServiceImpl extends ServiceImpl<CfgMethodMapper, CfgMethod>
implements CfgMethodService {
    @Override
    public boolean updateByBid(CfgMethod cfgMethod) {
        Assert.hasText(cfgMethod.getBid(),"bid is blank");
        LambdaUpdateWrapper<CfgMethod> condition = Wrappers.<CfgMethod>lambdaUpdate()
                .eq(CfgMethod::getBid, cfgMethod.getBid());
        return this.update(cfgMethod, condition);
    }

    /**
     * 根据bid获取数据
     *
     * @param bid
     * @return
     */
    @Override
    public CfgMethod getByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.getOne(Wrappers.<CfgMethod>lambdaQuery().eq(CfgMethod::getBid, bid));
    }

    /**
     * 分页查询
     *
     * @param pageQo
     * @return
     */
    @Override
    public PagedResult<CfgMethodVo> pageByCfgMethodQo(BaseRequest<CfgMethodQo> pageQo) {
        CfgMethod cfgMethod = CfgMethodConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgMethod> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        IPage<CfgMethod> iPage= this.page(page, Wrappers.query(cfgMethod));
        List<CfgMethodVo> cfgMethodVos = CfgMethodConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgMethodVos);
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
        return this.remove(Wrappers.<CfgMethod>lambdaQuery().eq(CfgMethod::getBid, bid));
    }
}
