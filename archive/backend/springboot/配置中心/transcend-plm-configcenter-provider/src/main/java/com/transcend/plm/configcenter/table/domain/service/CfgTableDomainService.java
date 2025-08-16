package com.transcend.plm.configcenter.table.domain.service;

import com.google.common.collect.Lists;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.application.service.impl.CfgObjectAppServiceImpl;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.table.infrastructure.repository.CfgTableRepository;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
import com.transcend.plm.configcenter.table.pojo.CfgTableAttributeConverter;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableAttributeDto;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 对象领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:39
 * @since 1.0
 */
@Service
public class CfgTableDomainService {
    @Resource
    private CfgTableRepository cfgTableRepository;

    @Resource
    private CfgObjectRepository cfgObjectRepository;

    public PagedResult<CfgTableVo> page(BaseRequest<CfgTableQo> pageQo) {
        return cfgTableRepository.page(pageQo);

    }

    public CfgTableVo getByBid(String bid) {
        return cfgTableRepository.getByBid(bid);
    }

    public CfgTableVo add(CfgTableDto dto) {
        return cfgTableRepository.add(dto);
    }

    public CfgTableVo update(CfgTableDto dto) {
        return cfgTableRepository.update(dto);
    }

    public boolean blukAddAttribute(List<CfgTableAttributeDto> attributeDtos) {
        List<CfgTableAttributePo> pos = CfgTableAttributeConverter.INSTANCE.dtos2pos(attributeDtos);
        return cfgTableRepository.blukAddAttribute(pos);
    }

    public int blukUpdateAttribute(List<CfgTableAttributeDto> attributeDtos) {
        return cfgTableRepository.blukUpdateAttribute(attributeDtos);
    }

    public CfgTableVo saveOrUpdate(CfgTableDto dto) {
        String dtoBid = dto.getBid();
        if (StringUtil.isBlank(dtoBid)) {
            return add(dto);
        }
        return update(dto);
    }

    public Boolean logicalDelete(String bid) {
        return cfgTableRepository.logicalDeleteByBid(bid);
    }

    public Boolean logicalDeleteAttributeByTableBid(String tableBid) {
        return cfgTableRepository.logicalDeleteByBid(tableBid);
    }

    public List<CfgTableVo> listByTableNamesAndEnableFlags(@NotNull List<String> tableNames, List<Integer> enableFlags) {
        return cfgTableRepository.listByTableNamesAndEnableFlags(tableNames, enableFlags);
    }

    public List<CfgTableAttributeVo> listDictionaryItemByDictionaryBids(Set<String> dictioanryBids) {
        return cfgTableRepository.listDictionaryItemByDictionaryBids(dictioanryBids);
    }

    public CfgTableVo getTableAndAttributeByBid(String bid) {
        // 1.获取表主信息
        CfgTableVo vo = getByBid(bid);
        // 2.获取表属性信息
        List<CfgTableAttributeVo> itemVos = listTableAttributesByTableBid(bid);
        vo.setAttributes(itemVos);
        return vo;
    }

    public CfgTableVo saveOrUpdateTableAndAttribute(CfgTableDto dto) {
        // 1.保存表
        CfgTableVo cfgTableVo = saveOrUpdate(dto);
        List<CfgTableAttributeDto> dictionaryItems = dto.getAttributes();
        List<CfgTableAttributeDto> cfgTableAttributeDtos = Lists.newArrayList();
        List<CfgTableAttributeDto> updateCfgDictionaryItemDtos = Lists.newArrayList();
        dictionaryItems.forEach(itemDto -> {
            // 设置表bid
            itemDto.setTableBid(cfgTableVo.getBid());
            // 1.新增item
            if (StringUtil.isBlank(itemDto.getBid())) {
                cfgTableAttributeDtos.add(itemDto);
                return;
            }
            // 2.更新item
            updateCfgDictionaryItemDtos.add(itemDto);
        });
        blukAddAttribute(cfgTableAttributeDtos);
        blukUpdateAttribute(updateCfgDictionaryItemDtos);
        return cfgTableVo;
    }

    public Boolean save(String bid, String type) {
        CfgTableVo tableVo = getTableAndAttributeByBid(bid);
        // 后期考虑分租户绑定对应的数据源 TODO


        return true;
    }

    public Boolean publishByType(String bid, String type) {
        CfgTableVo tableVo = getTableAndAttributeByBid(bid);
        // 后期考虑分租户绑定对应的数据源 TODO


        return true;
    }

    /**
     * 新增一个方法，用于获取表的属性列表，传入表的bid，返回表的属性列表
     */
    public List<CfgTableAttributeVo> listTableAttributesByTableBid(String tableBid) {
        List<CfgTableAttributePo> cfgTableAttributePos = cfgTableRepository.listTableAttributesByTableBid(tableBid);
        return CfgTableAttributeConverter.INSTANCE.pos2vos(cfgTableAttributePos);
    }

    public List<CfgTableAttributeVo> listTableAttributesByTableBids(List<String> tableBids) {
        List<CfgTableAttributePo> cfgTableAttributePos = cfgTableRepository.listTableAttributesByTableBids(tableBids);
        return CfgTableAttributeConverter.INSTANCE.pos2vos(cfgTableAttributePos);
    }

    public CfgTableVo getTableAndAttributeByModelCode(String modelCode) {
        //根据modelCode获取对象信息
        CfgObjectPo cfgObjectPo = cfgObjectRepository.getByModelCode(modelCode);
        //如果对象信息为空，抛出异常
        if (cfgObjectPo == null) {
            throw new BusinessException("对象信息不存在，modelCode:" + modelCode);
        }
        String tableBid = StringUtil.isNotBlank(cfgObjectPo.getBaseModel()) ? cfgObjectPo.getBaseModel() : modelCode;
        //查看对象是否已经发布
        CfgTableVo cfgTableVo = getByBid(tableBid);
        if (cfgTableVo == null) {
            throw new BusinessException("对象表信息不存在，tableBid:" + tableBid);
        }
        //获取表的属性信息
        List<CfgTableAttributeVo> cfgTableAttributeVos = listTableAttributesByTableBids(Lists.newArrayList(tableBid,cfgObjectPo.getType(), ObjectTypeEnum.ROOT.getCode()));
        cfgTableVo.setAttributes(cfgTableAttributeVos);
        return cfgTableVo;
    }
}
