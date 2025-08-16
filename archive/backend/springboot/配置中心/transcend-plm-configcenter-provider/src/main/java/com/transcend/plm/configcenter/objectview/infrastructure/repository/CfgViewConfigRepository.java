package com.transcend.plm.configcenter.objectview.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.dto.CfgViewConfigAttrDto;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.dto.CfgViewConfigDto;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo.CfgViewConfigQo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigAttrVo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigVo;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfig;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.po.CfgViewConfigAttr;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.service.CfgViewConfigAttrService;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.service.CfgViewConfigService;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class CfgViewConfigRepository {
    @Resource
    private CfgViewConfigService cfgViewConfigService;

    @Resource
    private CfgViewConfigAttrService cfgViewConfigAttrService;

    @Transactional(rollbackFor = Exception.class)
    public boolean add(CfgViewConfigDto cfgViewConfigDto) {
        List<CfgViewConfigAttrDto> cfgViewConfigAttrDtoList = cfgViewConfigDto.getViewAttrList();
        CfgViewConfig cfgViewConfig = BeanUtil.copy(cfgViewConfigDto, CfgViewConfig.class);
        cfgViewConfig.setBid(SnowflakeIdWorker.nextIdStr());
        if (!CollectionUtils.isEmpty(cfgViewConfigAttrDtoList)) {
            List<CfgViewConfigAttr> cfgViewConfigAttrs = BeanUtil.copy(cfgViewConfigAttrDtoList,
                CfgViewConfigAttr.class);
            for (CfgViewConfigAttr cfgViewConfigAttr : cfgViewConfigAttrs) {
                cfgViewConfigAttr.setViewBid(cfgViewConfig.getBid());
            }
            cfgViewConfigAttrService.saveBatch(cfgViewConfigAttrs);
        }
        return cfgViewConfigService.save(cfgViewConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean edit(CfgViewConfigDto cfgViewConfigDto) {
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getById(cfgViewConfigDto.getId());
        cfgViewConfig.setDescription(cfgViewConfigDto.getDescription());
        cfgViewConfig.setName(cfgViewConfigDto.getName());
        cfgViewConfig.setModelCode(cfgViewConfigDto.getModelCode());
        cfgViewConfig.setUpdatedTime(LocalDateTime.now());
        List<CfgViewConfigAttrDto> cfgViewConfigAttrDtoList = cfgViewConfigDto.getViewAttrList();
        cfgViewConfigAttrService
            .remove(Wrappers.<CfgViewConfigAttr> lambdaQuery().eq(CfgViewConfigAttr::getViewBid, cfgViewConfig.getBid())
                .lt(CfgViewConfigAttr::getCreatedTime, new Date()));
        if (!CollectionUtils.isEmpty(cfgViewConfigAttrDtoList)) {
            List<CfgViewConfigAttr> cfgViewConfigAttrs = BeanUtil.copy(cfgViewConfigAttrDtoList,
                CfgViewConfigAttr.class);
            for (CfgViewConfigAttr cfgViewConfigAttr : cfgViewConfigAttrs) {
                cfgViewConfigAttr.setViewBid(cfgViewConfig.getBid());
            }
            cfgViewConfigAttrService.saveBatch(cfgViewConfigAttrs);
        }
        return cfgViewConfigService.updateById(cfgViewConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean copy(CfgViewConfigDto cfgViewConfigDto) {
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getById(cfgViewConfigDto.getId());
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("view_bid", cfgViewConfig.getBid());
        List<CfgViewConfigAttr> cfgViewConfigAttrs = cfgViewConfigAttrService.listByMap(attrMap);
        cfgViewConfig.setName(cfgViewConfigDto.getName());
        cfgViewConfig.setDescription(cfgViewConfigDto.getDescription());
        cfgViewConfig.setEnableFlag(0);
        cfgViewConfig.setBid(SnowflakeIdWorker.nextIdStr());
        cfgViewConfig.setId(null);
        cfgViewConfig.setCreatedBy(null);
        cfgViewConfig.setUpdatedBy(null);
        cfgViewConfig.setCreatedTime(null);
        cfgViewConfig.setUpdatedTime(null);
        if (!CollectionUtils.isEmpty(cfgViewConfigAttrs)) {
            for (CfgViewConfigAttr cfgViewConfigAttr : cfgViewConfigAttrs) {
                cfgViewConfigAttr.setViewBid(cfgViewConfig.getBid());
                cfgViewConfigAttr.setBid(null);
                cfgViewConfigAttr.setId(null);
                cfgViewConfigAttr.setCreatedBy(null);
                cfgViewConfigAttr.setUpdatedBy(null);
                cfgViewConfigAttr.setCreatedTime(null);
                cfgViewConfigAttr.setUpdatedTime(null);
            }
            cfgViewConfigAttrService.saveBatch(cfgViewConfigAttrs);
        }
        return cfgViewConfigService.save(cfgViewConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String bid) {
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getOne(Wrappers.<CfgViewConfig> lambdaQuery().eq(CfgViewConfig::getBid,bid));
        Assert.isTrue((cfgViewConfig != null && cfgViewConfig.getEnableFlag() == 0),
            String.format("数据不存在或视图【%s】不是未启用状态，无法删除！", cfgViewConfig.getName()));
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("view_bid", cfgViewConfig.getBid());
        cfgViewConfigAttrService.removeByMap(attrMap);
        return cfgViewConfigService.removeById(cfgViewConfig.getId());
    }

    public CfgViewConfigVo getOne(String bid) {
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getOne(Wrappers.<CfgViewConfig> lambdaQuery().eq(CfgViewConfig::getBid,bid));
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("view_bid", cfgViewConfig.getBid());
        List<CfgViewConfigAttr> cfgViewConfigAttrs = cfgViewConfigAttrService.listByMap(attrMap);
        CfgViewConfigVo cfgViewConfigVo = BeanUtil.copy(cfgViewConfig, CfgViewConfigVo.class);
        List<CfgViewConfigAttrVo> cfgViewConfigAttrVos = BeanUtil.copy(cfgViewConfigAttrs, CfgViewConfigAttrVo.class);
        cfgViewConfigVo.setViewAttrList(cfgViewConfigAttrVos);
        return cfgViewConfigVo;
    }

    public List<CfgViewConfigVo> findViewByModelCode(String modelCode){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("model_code", modelCode);
        List<CfgViewConfig> list = cfgViewConfigService.listByMap(paramMap);
        List<CfgViewConfigVo> resList = BeanUtil.copy(list,CfgViewConfigVo.class);
        return resList;
    }

    public boolean setEnableFlag(CfgViewConfigDto cfgViewConfigDto) {
        Assert.notNull(cfgViewConfigDto.getEnableFlag(), "状态不能为空");
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getById(cfgViewConfigDto.getId());
        cfgViewConfig.setEnableFlag(cfgViewConfigDto.getEnableFlag());
        return cfgViewConfigService.updateById(cfgViewConfig);
    }

    public boolean editViewInfo(CfgViewConfigDto cfgViewConfigDto){
        CfgViewConfig cfgViewConfig = cfgViewConfigService.getById(cfgViewConfigDto.getId());
        cfgViewConfig.setRoleCode(cfgViewConfigDto.getRoleCode());
        cfgViewConfig.setRoleType(cfgViewConfigDto.getRoleType());
        cfgViewConfig.setTags(cfgViewConfigDto.getTags());
        cfgViewConfig.setPriority(cfgViewConfigDto.getPriority());
        cfgViewConfig.setLcStateCode(cfgViewConfigDto.getLcStateCode());
        return cfgViewConfigService.updateById(cfgViewConfig);
    }

    public PagedResult<CfgViewConfigVo> page(BaseRequest<CfgViewConfigQo> pageQo) {
        return cfgViewConfigService.page(pageQo);
    }
}
