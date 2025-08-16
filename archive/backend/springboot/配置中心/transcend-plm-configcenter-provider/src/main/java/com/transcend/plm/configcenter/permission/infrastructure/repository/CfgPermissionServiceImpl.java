package com.transcend.plm.configcenter.permission.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.CfgObject1PermissionOperationMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.CfgPermissionPo;
import com.transcend.plm.configcenter.permission.pojo.CfgAttributeConverter;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgPermissionServiceImpl extends ServiceImpl<CfgObject1PermissionOperationMapper, CfgPermissionPo>
implements CfgPermissionService {

    /**
     * 根据bid进行更新
     *
     * @param cfgPermissionPo
     * @return
     */
    @Override
    public CfgPermissionPo updateByBid(CfgPermissionPo cfgPermissionPo) {
        Assert.hasText(cfgPermissionPo.getBid(),"bid is blank");
        this.update(cfgPermissionPo,Wrappers.<CfgPermissionPo>lambdaUpdate()
                .eq(CfgPermissionPo::getBid, cfgPermissionPo.getBid())
        );
        return cfgPermissionPo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgPermissionPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgPermissionPo>lambdaQuery().eq(CfgPermissionPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgAttributeVo> pageByCfgAttributeQo(BaseRequest<CfgObjectPermissionOperationQo> pageQo) {
        CfgPermissionPo cfgPermissionPo = CfgAttributeConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgPermissionPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgPermissionPo> queryWrapper = Wrappers.<CfgPermissionPo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getCode()), CfgPermissionPo::getCode, cfgPermissionPo.getCode())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getName()), CfgPermissionPo::getName, cfgPermissionPo.getName())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getGroupName()), CfgPermissionPo::getGroupName, cfgPermissionPo.getGroupName())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getDataType()), CfgPermissionPo::getDataType, cfgPermissionPo.getDataType())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getDbKey()), CfgPermissionPo::getDbKey, cfgPermissionPo.getDbKey())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getCreatedBy()), CfgPermissionPo::getCreatedBy, cfgPermissionPo.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgPermissionPo.getUpdatedBy()), CfgPermissionPo::getUpdatedBy, cfgPermissionPo.getUpdatedBy());
        IPage<CfgPermissionPo> iPage = this.page(page,queryWrapper);
        List<CfgAttributeVo> cfgAttributeVos = CfgAttributeConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgAttributeVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_attribute", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgPermissionPo>lambdaQuery().eq(CfgPermissionPo::getBid, bid));
    }
}
