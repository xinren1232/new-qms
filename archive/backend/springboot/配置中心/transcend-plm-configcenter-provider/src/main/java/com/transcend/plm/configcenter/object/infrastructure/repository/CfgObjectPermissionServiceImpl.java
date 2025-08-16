package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectPermissionMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectPermissionConverter;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Service
public class CfgObjectPermissionServiceImpl extends ServiceImpl<CfgObjectPermissionMapper, CfgObjectPermissionPo>
implements CfgObjectPermissionService {

    /**
     * 根据bid进行更新
     *
     * @param cfgObjectPermissionPo
     * @return
     */
    @Override
    public CfgObjectPermissionPo updateByBid(CfgObjectPermissionPo cfgObjectPermissionPo) {
        Assert.hasText(cfgObjectPermissionPo.getBid(),"bid is blank");
        this.update(cfgObjectPermissionPo,Wrappers.<CfgObjectPermissionPo>lambdaUpdate()
                .eq(CfgObjectPermissionPo::getBid, cfgObjectPermissionPo.getBid())
        );
        return cfgObjectPermissionPo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgObjectPermissionPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgObjectPermissionPo>lambdaQuery().eq(CfgObjectPermissionPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgObjectPermissionVo> pageByQo(BaseRequest<CfgObjectPermissionQo> pageQo) {
        CfgObjectPermissionPo cfgObjectPermissionPo = CfgObjectPermissionConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgObjectPermissionPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgObjectPermissionPo> queryWrapper = Wrappers.<CfgObjectPermissionPo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgObjectPermissionPo.getModelCode()), CfgObjectPermissionPo::getModelCode, cfgObjectPermissionPo.getModelCode())
                .eq(StringUtil.isNotBlank(cfgObjectPermissionPo.getBaseModel()), CfgObjectPermissionPo::getBaseModel, cfgObjectPermissionPo.getBaseModel())
                .eq(StringUtil.isNotBlank(cfgObjectPermissionPo.getCreatedBy()), CfgObjectPermissionPo::getCreatedBy, cfgObjectPermissionPo.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgObjectPermissionPo.getUpdatedBy()), CfgObjectPermissionPo::getUpdatedBy, cfgObjectPermissionPo.getUpdatedBy());
        IPage<CfgObjectPermissionPo> iPage = this.page(page,queryWrapper);
        List<CfgObjectPermissionVo> cfgObjectPermissionVos = CfgObjectPermissionConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgObjectPermissionVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_object_permission", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgObjectPermissionPo>lambdaQuery().eq(CfgObjectPermissionPo::getBid, bid));
    }

}
