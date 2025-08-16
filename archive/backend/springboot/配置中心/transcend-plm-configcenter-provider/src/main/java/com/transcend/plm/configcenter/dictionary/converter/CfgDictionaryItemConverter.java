package com.transcend.plm.configcenter.dictionary.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryItemPo;
import com.transsion.framework.common.CollectionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-25 11:06
 **/
@Mapper
public interface CfgDictionaryItemConverter {

    /**
     * 暂时只支持中文和英文，后期可以做扩展
     */
    Set<String> languageSets = Sets.newHashSet("zh","en");

    CfgDictionaryItemConverter INSTANCE = Mappers.getMapper(CfgDictionaryItemConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgDictionaryItemPo dto2po(CfgDictionaryItemDto dto);


    /**
     * dto 转化为 do
     * @param cfgDictionaryItemPos
     * @return
     */
    CfgDictionaryItemVo po2vo(CfgDictionaryItemPo cfgDictionaryItemPos);

    /**
     * do 转化为 vo
     * @param cfgDictionaryItemPos
     * @return
     */
    default List<CfgDictionaryItemVo> pos2vos(List<CfgDictionaryItemPo> cfgDictionaryItemPos){
        if (CollectionUtils.isEmpty(cfgDictionaryItemPos)){
            return Lists.newArrayList();
        }
        return cfgDictionaryItemPos.stream().map(po -> {
            CfgDictionaryItemVo vo = po2vo(po);
            vo.putAll(po.getMultilingualValueMap());
            return vo;
        }).collect(Collectors.toList());
    }


    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    default List<CfgDictionaryItemPo> dtos2pos(List<CfgDictionaryItemDto> cfgAttributeDtos){
        return cfgAttributeDtos.stream().map(dto -> {
            CfgDictionaryItemPo po = dto2po(dto);
            final Map<String, String> multilingualValueMap =
                    CollectionUtil.isNotEmpty(po.getMultilingualValueMap()) ? po.getMultilingualValueMap() : new HashMap<>();
            po.setMultilingualValueMap(multilingualValueMap);
            dto.forEach((k, v)->{
                if (languageSets.contains(k)){
                    multilingualValueMap.put(k, (String)v);
                }
            });
            return po;
        }).collect(Collectors.toList());
    }


}
