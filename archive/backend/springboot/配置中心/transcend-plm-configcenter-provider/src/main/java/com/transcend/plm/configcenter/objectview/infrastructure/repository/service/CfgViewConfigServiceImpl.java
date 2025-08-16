package com.transcend.plm.configcenter.objectview.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo.CfgViewConfigQo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigVo;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.mapper.CfgViewConfigMapper;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfig;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class CfgViewConfigServiceImpl extends ServiceImpl<CfgViewConfigMapper, CfgViewConfig>
    implements CfgViewConfigService {

    @Override
    public PagedResult<CfgViewConfigVo> page(BaseRequest<CfgViewConfigQo> pageQo) {
        CfgViewConfigQo cfgViewConfigQo = pageQo.getParam();
        Page<CfgViewConfig> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgViewConfig> queryWrapper = Wrappers.<CfgViewConfig> lambdaQuery();
        if (StringUtil.isNotBlank(cfgViewConfigQo.getName())) {
            queryWrapper.like(CfgViewConfig::getName, cfgViewConfigQo.getName());
        }
        if (cfgViewConfigQo.getEnableFlag() != null) {
            queryWrapper.eq(CfgViewConfig::getEnableFlag, cfgViewConfigQo.getEnableFlag());
        }
        queryWrapper.eq(CfgViewConfig::getDeleteFlag, 0);
        if (StringUtil.isNotBlank(cfgViewConfigQo.getModelCode())) {
            queryWrapper.like(CfgViewConfig::getModelCode, cfgViewConfigQo.getModelCode());
        }
        IPage<CfgViewConfig> iPage = this.page(page, queryWrapper);
        List<CfgViewConfigVo> cfgViewConfigVos = BeanUtil.copy(iPage.getRecords(), CfgViewConfigVo.class);
        return PageResultTools.create(iPage, cfgViewConfigVos);
    }
}
