package com.transcend.plm.configcenter.object.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.constant.DataBaseConstant;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgModelEventMethodPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgModelEventMethodService;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgModelEventMethodMapper;
import com.transcend.plm.configcenter.object.pojo.CfgModelEventMethodConverter;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
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
public class CfgModelEventMethodServiceImpl extends ServiceImpl<CfgModelEventMethodMapper, CfgModelEventMethodPo>
implements CfgModelEventMethodService {



    @Override
    public boolean updateByBid(CfgModelEventMethodPo cfgModelEventMethod) {
        Assert.hasText(cfgModelEventMethod.getBid(),"bid is blank");
        LambdaUpdateWrapper<CfgModelEventMethodPo> condition = Wrappers.<CfgModelEventMethodPo>lambdaUpdate()
                .eq(CfgModelEventMethodPo::getBid, cfgModelEventMethod.getBid());
        return this.update(cfgModelEventMethod, condition);
    }

    /**
     * 根据bid获取数据
     *
     * @param bid
     * @return
     */
    @Override
    public CfgModelEventMethodPo getByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.getOne(Wrappers.<CfgModelEventMethodPo>lambdaQuery().eq(CfgModelEventMethodPo::getBid, bid));
    }

    /**
     * 分页查询
     *
     * @param pageQo
     * @return
     */
    @Override
    public PagedResult<CfgModelEventMethodVo> pageByCfgModelEventMethodQo(BaseRequest<CfgModelEventMethodQo> pageQo){
        CfgModelEventMethodPo cfgModelEventMethod = CfgModelEventMethodConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgModelEventMethodPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        IPage<CfgModelEventMethodPo> iPage= this.page(page, Wrappers.query(cfgModelEventMethod));
        List<CfgModelEventMethodVo> cfgModelEventMethodVos = CfgModelEventMethodConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgModelEventMethodVos);
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
        return this.remove(Wrappers.<CfgModelEventMethodPo>lambdaQuery().eq(CfgModelEventMethodPo::getBid, bid));
    }

    /**
     * 获取列表
     *
     * @param cfgModelEventMethod
     * @return
     */
    @Override
    public List<CfgModelEventMethodPo> findByCondition(CfgModelEventMethodPo cfgModelEventMethod) {
        return this.list(Wrappers.lambdaQuery(cfgModelEventMethod));
    }

    /**
     * 批量新增
     *
     * @param dtos
     * @return
     */
    @Override
    public List<CfgModelEventMethodPo> bulkAdd(List<CfgModelEventMethodPo> dtos) {
        this.saveBatch(dtos, DataBaseConstant.BATCH_SIZE);
        return dtos;
    }

    /**
     * 批量删除
     *
     * @param bids
     * @return
     */
    @Override
    public Boolean bulkDelete(List<String> bids) {
        return this.remove(Wrappers.<CfgModelEventMethodPo>lambdaQuery().in(CfgModelEventMethodPo::getBid, bids));
    }
}
