package com.transcend.plm.configcenter.role.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.role.infrastructure.repository.mapper.CfgRoleMapper;
import com.transcend.plm.configcenter.role.infrastructure.repository.po.CfgRolePo;
import com.transcend.plm.configcenter.role.pojo.CfgRoleConverter;
import com.transcend.plm.configcenter.role.pojo.qo.CfgRoleQo;
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
public class CfgRoleServiceImpl extends ServiceImpl<CfgRoleMapper, CfgRolePo>
implements CfgRoleService {

    /**
     * 根据bid进行更新
     *
     * @param cfgRolePo
     * @return
     */
    @Override
    public CfgRolePo updateByBid(CfgRolePo cfgRolePo) {
        this.update(cfgRolePo,Wrappers.<CfgRolePo>lambdaUpdate()
                .eq(CfgRolePo::getBid, cfgRolePo.getBid())
        );
        return cfgRolePo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgRolePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgRolePo>lambdaQuery().eq(CfgRolePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgRoleVo> pageByCfgAttributeQo(BaseRequest<CfgRoleQo> pageQo) {
        CfgRolePo cfgAttribute = CfgRoleConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgRolePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgRolePo> queryWrapper = Wrappers.<CfgRolePo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgAttribute.getParentBid()), CfgRolePo::getParentBid, cfgAttribute.getParentBid())
                .eq(StringUtil.isNotBlank(cfgAttribute.getCode()), CfgRolePo::getCode, cfgAttribute.getCode())
                .eq(StringUtil.isNotBlank(cfgAttribute.getDescription()), CfgRolePo::getDescription, cfgAttribute.getDescription())
                .eq(StringUtil.isNotBlank(cfgAttribute.getName()), CfgRolePo::getName, cfgAttribute.getName())
                .eq(StringUtil.isNotBlank(cfgAttribute.getCreatedBy()), CfgRolePo::getCreatedBy, cfgAttribute.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgAttribute.getUpdatedBy()), CfgRolePo::getUpdatedBy, cfgAttribute.getUpdatedBy());
        IPage<CfgRolePo> iPage = this.page(page,queryWrapper);
        List<CfgRoleVo> cfgAttributeVos = CfgRoleConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgAttributeVos);
    }


    @Override
    @CheckTableDataExist(tableName = "cfg_role",fieldName = "bid",exist = true)
    public Boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgRolePo>lambdaQuery().eq(CfgRolePo::getBid, bid));
    }

    @Override
    public List<CfgRolePo> queryByCodes(List<String> codeList) {
        LambdaQueryWrapper<CfgRolePo> queryWrapper = Wrappers.<CfgRolePo>lambdaQuery()
                .in(CfgRolePo::getCode, codeList)
                .eq(CfgRolePo::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                .eq(CfgRolePo::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED);
        return this.list(queryWrapper);
    }
}
