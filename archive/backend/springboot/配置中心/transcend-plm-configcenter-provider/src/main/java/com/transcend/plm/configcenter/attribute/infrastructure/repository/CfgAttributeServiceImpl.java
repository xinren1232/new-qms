package com.transcend.plm.configcenter.attribute.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.attribute.infrastructure.repository.mapper.CfgAttributeMapper;
import com.transcend.plm.configcenter.attribute.infrastructure.repository.po.CfgAttributePo;
import com.transcend.plm.configcenter.attribute.pojo.CfgAttributeConverter;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgAttributeServiceImpl extends ServiceImpl<CfgAttributeMapper, CfgAttributePo>
implements CfgAttributeService{

    /**
     * 根据bid进行更新
     *
     * @param cfgAttributePo
     * @return
     */
    @Override
    public CfgAttributePo updateByBid(CfgAttributePo cfgAttributePo) {
        Assert.hasText(cfgAttributePo.getBid(),"bid is blank");
        this.update(cfgAttributePo,Wrappers.<CfgAttributePo>lambdaUpdate()
                .eq(CfgAttributePo::getBid, cfgAttributePo.getBid())
        );
        return cfgAttributePo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgAttributePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgAttributePo>lambdaQuery().eq(CfgAttributePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgAttributeVo> pageByCfgAttributeQo(BaseRequest<CfgAttributeQo> pageQo) {
        CfgAttributePo cfgAttributePo = CfgAttributeConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgAttributePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgAttributePo> queryWrapper = Wrappers.<CfgAttributePo>lambdaQuery()
                .eq(cfgAttributePo.getEnableFlag() != null, CfgAttributePo::getEnableFlag, cfgAttributePo.getEnableFlag())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getCode()), CfgAttributePo::getCode, cfgAttributePo.getCode())
                .like(StringUtil.isNotBlank(cfgAttributePo.getName()), CfgAttributePo::getName, cfgAttributePo.getName())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getGroupName()), CfgAttributePo::getGroupName, cfgAttributePo.getGroupName())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getDataType()), CfgAttributePo::getDataType, cfgAttributePo.getDataType())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getDbKey()), CfgAttributePo::getDbKey, cfgAttributePo.getDbKey())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getCreatedBy()), CfgAttributePo::getCreatedBy, cfgAttributePo.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgAttributePo.getUpdatedBy()), CfgAttributePo::getUpdatedBy, cfgAttributePo.getUpdatedBy());
        IPage<CfgAttributePo> iPage = this.page(page,queryWrapper);
        List<CfgAttributeVo> cfgAttributeVos = CfgAttributeConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgAttributeVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_attribute", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgAttributePo>lambdaQuery().eq(CfgAttributePo::getBid, bid));
    }

    public List<CfgAttributeVo> listAll(){
        List<CfgAttributePo> cfgAttributePos = this.list(Wrappers.<CfgAttributePo>lambdaQuery().eq(CfgAttributePo::getDeleteFlag, 0));
        return CfgAttributeConverter.INSTANCE.pos2vos(cfgAttributePos);
    }

    @Override
    public CfgAttributePo getByCode(String code) {
        Assert.hasText(code, "code is blank");
        return this.getOne(Wrappers.<CfgAttributePo>lambdaQuery().eq(CfgAttributePo::getCode, code)
                .eq(CfgAttributePo::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED).eq(CfgAttributePo::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE));
    }
}
