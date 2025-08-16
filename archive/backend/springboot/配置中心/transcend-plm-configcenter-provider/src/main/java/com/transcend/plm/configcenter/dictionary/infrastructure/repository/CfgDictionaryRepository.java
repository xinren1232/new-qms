package com.transcend.plm.configcenter.dictionary.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryDetail;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.common.config.CommonProperties;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.exception.PlmDataException;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper.CfgDictionaryItemMapper;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper.CfgDictionaryMapper;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryItemPo;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import com.transcend.plm.configcenter.dictionary.converter.CfgDictionaryConverter;
import com.transcend.plm.configcenter.dictionary.converter.CfgDictionaryItemConverter;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 动态模型资源库
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2021/9/22 17:21
 * @since 1.0
 */
@Slf4j
@Repository
public class CfgDictionaryRepository {

    @Resource
    private CfgDictionaryService cfgDictionaryService;
    @Resource
    private CommonProperties commonProperties;

    @Resource
    private CfgDictionaryItemService cfgDictionaryItemService;

    @Resource
    private CfgDictionaryItemMapper cfgDictionaryItemMapper;

    @Resource
    private CfgDictionaryMapper cfgDictionaryMapper;

    public PagedResult<CfgDictionaryVo> page(BaseRequest<CfgDictionaryQo> pageQo) {
        return cfgDictionaryService.pageByQo(pageQo);
    }

    public CfgDictionaryVo getByBid(String bid) {
        CfgDictionaryPo po = cfgDictionaryService.getByBid(bid);
        return CfgDictionaryConverter.INSTANCE.po2vo(po);
    }

    public CfgDictionaryVo add(CfgDictionaryDto dto) {
        CfgDictionaryPo dictionaryPo = CfgDictionaryConverter.INSTANCE.dto2po(dto);
        cfgDictionaryService.save(dictionaryPo);
        return CfgDictionaryConverter.INSTANCE.po2vo(dictionaryPo);
    }

    public List<CfgDictionaryVo> addBatch(List<CfgDictionaryDto> dtos) {
        List<CfgDictionaryPo> dictionaryPos = CfgDictionaryConverter.INSTANCE.dto2po(dtos);
        cfgDictionaryService.saveBatch(dictionaryPos);
        return CfgDictionaryConverter.INSTANCE.po2vo(dictionaryPos);
    }

    public CfgDictionaryVo update(CfgDictionaryDto dto) {
        CfgDictionaryPo dictionaryPo = CfgDictionaryConverter.INSTANCE.dto2po(dto);
        cfgDictionaryService.updateByBid(dictionaryPo);
        return CfgDictionaryConverter.INSTANCE.po2vo(dictionaryPo);
    }

    public boolean blukAddDictionaryItem(List<CfgDictionaryItemDto> dictionaryItems) {
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return false;
        }
        // Key重复校验
        // 先检查当前新增item中的key是否有重复
        Set<String> addKeys = dictionaryItems.stream().map(CfgDictionaryItemDto::getKeyCode).collect(Collectors.toSet());
        if (addKeys.size() != dictionaryItems.size()) {
            throw new PlmDataException("新增字典项中存在重复的key");
        }
        // 检查已存在的key是否有重复
        List<CfgDictionaryItemPo> existKeyItems =
                cfgDictionaryItemService.list(Wrappers.<CfgDictionaryItemPo>lambdaQuery()
                        .in(CfgDictionaryItemPo::getKeyCode, addKeys)
                        .eq(CfgDictionaryItemPo::getDeleteFlag, 0));
        if (CollUtil.isNotEmpty(existKeyItems)) {
            String keysStr = existKeyItems.stream()
                    .map(CfgDictionaryItemPo::getKeyCode)
                    .collect(Collectors.joining(","));
            throw new PlmDataException("添加字典项中存在重复的key，重复的Key为：" + keysStr + "，请替换后重新添加");
        }
        List<CfgDictionaryItemPo> pos = CfgDictionaryItemConverter.INSTANCE.dtos2pos(dictionaryItems);
        return cfgDictionaryItemService.saveBatch(pos);
    }

    public int blukUpdateDictionaryItem(List<CfgDictionaryItemDto> dictionaryItems) {
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return 0;
        }
        List<CfgDictionaryItemPo> pos = CfgDictionaryItemConverter.INSTANCE.dtos2pos(dictionaryItems);
        return cfgDictionaryItemMapper.blukUpdateByBid(pos);
    }

    public List<CfgDictionaryItemVo> listDictionaryItemByDictionaryBid(String bid) {
        List<CfgDictionaryItemPo> pos = cfgDictionaryItemMapper.listDictionaryItemByDictionaryBids(Sets.newHashSet(bid), null);
        return CfgDictionaryItemConverter.INSTANCE.pos2vos(pos);
    }

    public CfgDictionaryVo getDictionaryByCode(String code) {
        CfgDictionaryPo po = cfgDictionaryMapper.getByCode(code);
        if (po == null) {
            return null;
        }
        return CfgDictionaryConverter.INSTANCE.po2vo(po);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgDictionaryService.logicalDeleteByBid(bid);
    }

    public List<CfgDictionaryVo> listByByCodesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags) {
        // codes 不能为空
        if (CollectionUtils.isEmpty(codes)) {
            return Lists.newArrayList();
        }
        // codes 数量限制为1000
        if (commonProperties.getDbInLimit() < codes.size()) {
            throw new BusinessException(ErrorMsgEnum.DICT_ERROR_CODES_LIMIT.getCode(),
                    ErrorMsgEnum.DICT_ERROR_CODES_LIMIT.getDesc());
        }
        List<CfgDictionaryPo> pos = cfgDictionaryMapper.listByByCodesAndEnableFlags(codes, enableFlags);
        // 转换
        return CfgDictionaryConverter.INSTANCE.pos2vos(pos);
    }

    public List<CfgDictionaryItemVo> listDictionaryItemByDictionaryBids(Set<String> dictioanryBids) {
        List<CfgDictionaryItemPo> pos = cfgDictionaryItemMapper.listDictionaryItemByDictionaryBids(dictioanryBids, null);
        return CfgDictionaryItemConverter.INSTANCE.pos2vos(pos);
    }

    public List<CfgDictionaryItemVo> listDictionaryItemByDictionaryName(String name) {
        List<CfgDictionaryItemPo> pos = cfgDictionaryItemMapper.listDictionaryItemByDictionaryName(name);
        return CfgDictionaryItemConverter.INSTANCE.pos2vos(pos);
    }

    public List<CfgDictionaryDetail> listDictionaryDetails(){
        return cfgDictionaryItemMapper.listDictionaryDetails();
    }
}
