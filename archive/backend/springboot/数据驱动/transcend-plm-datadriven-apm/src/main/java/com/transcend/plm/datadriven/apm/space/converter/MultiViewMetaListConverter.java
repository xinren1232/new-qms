package com.transcend.plm.datadriven.apm.space.converter;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiViewMetaListVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ViewMetaListVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ViewMetaVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.mapping.MappingContext;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 多视图元数据列表转换器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/23 13:43
 */
@Mapper
public interface MultiViewMetaListConverter {
    MultiViewMetaListConverter INSTANCE = Mappers.getMapper(MultiViewMetaListConverter.class);

    /**
     * 转换为多视图元数据
     *
     * @param metaDto    视图元数据
     * @param properties 字段参数
     * @return 多视图元数据
     */
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "properties", expression = "java(properties)")
    @Mapping(target = "remoteDictTypes", expression = "java(remoteDictTypeToList(metaDto.getRemoteDictType()))")
    ViewMetaVo toMetaVo(CfgViewMetaDto metaDto, @Context Map<String, Object> properties);

    /**
     * 转换为元数据列表
     *
     * @param metaDtoList   视图元数据列表
     * @param propertiesMap 字段参数
     * @return 多视图元数据列表
     */
    default List<ViewMetaVo> toMetaVoList(List<CfgViewMetaDto> metaDtoList,
                                          @Context Map<String, Map<String, Object>> propertiesMap) {
        if (CollUtil.isEmpty(metaDtoList)) {
            return Collections.emptyList();
        }
        return metaDtoList.stream().filter(Objects::nonNull)
                .map(metaDto -> toMetaVo(metaDto, propertiesMap.get(getOnlyKey(metaDto))))
                .collect(Collectors.toList());
    }

    /**
     * 转换为多字典列表
     *
     * @param remoteDictType 远程字典类型
     * @return 多字典列表
     */
    default List<String> remoteDictTypeToList(String remoteDictType) {
        if (remoteDictType == null) {
            return null;
        }
        List<String> remoteDictTypes = new ArrayList<>();
        remoteDictTypes.add(remoteDictType);
        return remoteDictTypes;
    }

    /**
     * 转换为多视图元数据
     *
     * @param cfgViewVo     视图配置信息
     * @param viewModelType 视图模式类型
     * @return 多视图元数据
     */
    default MultiViewMetaListVo viewToMultiMetaVo(CfgViewVo cfgViewVo, String viewModelType) {
        if (cfgViewVo == null) {
            return null;
        }
        MultiViewMetaListVo vo = new MultiViewMetaListVo();
        vo.setViewModelType(viewModelType);
        vo.setModelCodeList(Collections.singletonList(cfgViewVo.getModelCode()));
        vo.setSpaceAppBidList(Collections.singletonList(cfgViewVo.getBelongBid()));
        vo.setViewMetaList(toMetaVoList(cfgViewVo.getMetaList(), getPropertiesMap(cfgViewVo)));
        return vo;
    }

    /**
     * 转换为多视图元数据
     *
     * @param cfgViewVo     视图配置信息
     * @param viewModelType 视图模式类型
     * @return 多视图元数据
     */
    default ViewMetaListVo viewToMetaVo(CfgViewVo cfgViewVo, String viewModelType) {
        if (cfgViewVo == null) {
            return null;
        }
        ViewMetaListVo vo = new ViewMetaListVo();
        vo.setViewModelType(viewModelType);
        vo.setModelCode(cfgViewVo.getModelCode());
        vo.setSpaceAppBid(cfgViewVo.getBelongBid());
        vo.setViewMetaList(toMetaVoList(cfgViewVo.getMetaList(), getPropertiesMap(cfgViewVo)));
        return vo;
    }

    /**
     * 转换为多视图元数据列表
     *
     * @param cfgViewVos    视图配置信息列表
     * @param viewModelType 视图模式类型
     * @return 多视图元数据列表
     */
    default List<ViewMetaListVo> listViewToMetaVo(List<CfgViewVo> cfgViewVos, String viewModelType) {
        if (cfgViewVos == null) {
            return null;
        }
        List<ViewMetaListVo> metaList = cfgViewVos.stream().map(cfgViewVo -> viewToMetaVo(cfgViewVo, viewModelType))
                .sorted(Comparator.comparing(ViewMetaListVo::getModelCode))
                .collect(Collectors.toList());
        //写入应用名称
        MappingContext<ViewMetaListVo, String, String> appNameContext = getSpaceAppNameContext(metaList);
        metaList.forEach(metaVo -> metaVo.setSpaceAppName(appNameContext.get(metaVo)));
        //追加全局元数据信息
        return additionalGlobalMetaData(viewModelType, metaList);
    }

    /**
     * 转换为多视图元数据
     *
     * @param cfgViewVos    视图配置信息列表
     * @param viewModelType 视图模式类型
     * @return 多视图元数据
     */
    default MultiViewMetaListVo viewListToMetaVo(Collection<CfgViewVo> cfgViewVos, String viewModelType) {
        if (cfgViewVos == null) {
            return null;
        }
        MultiViewMetaListVo vo = new MultiViewMetaListVo();
        vo.setViewModelType(viewModelType);
        vo.setModelCodeList(cfgViewVos.stream().map(CfgViewVo::getModelCode).collect(Collectors.toList()));
        vo.setSpaceAppBidList(cfgViewVos.stream().map(CfgViewVo::getBelongBid).collect(Collectors.toList()));

        // 使用Map来存储合并后的MetaList，以name+label为key
        Map<String, ViewMetaVo> metaMap = new HashMap<>(16);

        int order = 0;
        // 遍历所有视图的MetaList进行合并
        for (CfgViewVo cfgViewVo : cfgViewVos) {
            Map<String, Map<String, Object>> propertiesMap = getPropertiesMap(cfgViewVo);
            if (cfgViewVo.getMetaList() != null) {
                for (CfgViewMetaDto metaDto : cfgViewVo.getMetaList()) {
                    String onlyKey = getOnlyKey(metaDto);
                    ViewMetaVo existingMeta = metaMap.get(onlyKey);
                    ViewMetaVo otherMeta = toMetaVo(metaDto, propertiesMap.get(onlyKey));
                    if (existingMeta == null) {
                        // 如果不存在
                        otherMeta.setOrder(order++);
                        metaMap.put(onlyKey, otherMeta);
                    } else {
                        mergeViewMeta(existingMeta, otherMeta);
                    }
                }
            }
        }

        vo.setViewMetaList(metaMap.values().stream()
                //排序
                .sorted(Comparator.comparingInt(ViewMetaVo::getOrder))
                .collect(Collectors.toList()));
        return vo;
    }

    /**
     * 合并ViewMetaVo对象
     *
     * @param existingMeta 已经存在的对象
     * @param otherMeta    不存在的对象
     */
    static void mergeViewMeta(ViewMetaVo existingMeta, ViewMetaVo otherMeta) {

        // 如果已存在，只合并remoteDictTypes和optionItems
        if (CollUtil.isNotEmpty(otherMeta.getRemoteDictTypes())) {
            List<String> remoteDictTypes = existingMeta.getRemoteDictTypes();
            if (remoteDictTypes == null) {
                remoteDictTypes = new ArrayList<>();
                existingMeta.setRemoteDictTypes(remoteDictTypes);
            }
            for (String item : otherMeta.getRemoteDictTypes()) {
                if (existingMeta.getRemoteDictTypes().stream().noneMatch(i -> i.equals(item))) {
                    existingMeta.getRemoteDictTypes().add(item);
                }
            }
        }

        // 合并optionItems
        if (CollUtil.isNotEmpty(otherMeta.getOptionItems())) {
            if (existingMeta.getOptionItems() == null) {
                existingMeta.setOptionItems(new ArrayList<>());
            }
            for (CfgOptionItemDto item : otherMeta.getOptionItems()) {
                if (existingMeta.getOptionItems().stream().noneMatch(i -> i.getLabel().equals(item.getLabel()))) {
                    existingMeta.getOptionItems().add(item);
                }
            }
        }
    }

    /**
     * 获取唯一key
     *
     * @param metaDto 元数据对象
     * @return 唯一key
     */
    @NotNull
    static String getOnlyKey(CfgViewMetaDto metaDto) {
        return String.format("%s_:_%s", metaDto.getName(), metaDto.getLabel());
    }

    /**
     * 获取唯一key
     *
     * @param metaVo 元数据对象
     * @return 唯一key
     */
    @NotNull
    static String getOnlyKey(ViewMetaVo metaVo) {
        return String.format("%s_:_%s", metaVo.getName(), metaVo.getLabel());
    }

    /**
     * 获取唯一key
     *
     * @param metaDto 源数据对象
     * @return 唯一key
     */
    @NotNull
    @SuppressWarnings("unchecked")
    static String getOnlyKey(Map<String, Object> metaDto) {
        Object label = Optional.ofNullable(metaDto.get("field")).map(field -> (Map<String, Object>) field)
                .map(field -> field.get("options")).map(options -> (Map<String, Object>) options)
                .map(options -> options.get("label")).orElse("");
        return String.format("%s_:_%s", metaDto.get("name"), label);
    }

    /**
     * 获取字段参数对应map
     *
     * @param cfgViewVo 视图配置
     * @return 字段参数Map
     */
    @SuppressWarnings("unchecked")
    static Map<String, Map<String, Object>> getPropertiesMap(CfgViewVo cfgViewVo) {
        return Optional.ofNullable(cfgViewVo.getContent())
                .map(content -> content.get("propertiesList")).map(propertiesList -> (List<Map<String, Object>>) propertiesList)
                .map(list -> list.stream().collect(Collectors.toMap(MultiViewMetaListConverter::getOnlyKey, Function.identity(), (m1, m2) -> m1)))
                .orElse(Collections.emptyMap());
    }

    /**
     * 添加全局元数据信息
     *
     * @param viewModelType 视图模式类型
     * @param metaList      元数据列表
     * @return 元数据列表
     */
    @Nullable
    static List<ViewMetaListVo> additionalGlobalMetaData(String viewModelType, List<ViewMetaListVo> metaList) {
        int viewMetaListSize = metaList.size();
        if (metaList.isEmpty() || viewMetaListSize == 1) {
            return metaList;
        }
        List<ViewMetaVo> globalMetaDataList = metaList.stream()
                .map(ViewMetaListVo::getViewMetaList).flatMap(Collection::stream)
                .collect(Collectors.groupingBy(MultiViewMetaListConverter::getOnlyKey))
                .values().stream()
                .filter(list -> list.size() == viewMetaListSize)
                .map(list -> {
                    ViewMetaVo viewMetaVo = list.remove(0);
                    //合并信息
                    list.forEach(item -> mergeViewMeta(viewMetaVo, item));
                    return viewMetaVo;
                }).collect(Collectors.toList());

        ViewMetaListVo globalMetaData = new ViewMetaListVo();
        globalMetaData.setViewModelType(viewModelType);
        globalMetaData.setViewMetaList(globalMetaDataList);
        metaList.add(0, globalMetaData);

        return metaList;
    }

    /**
     * 获取空间应用名称上下文
     *
     * @param metaList 元数据列表
     * @return 空间应用名称上下文
     */
    static MappingContext<ViewMetaListVo, String, String> getSpaceAppNameContext(List<ViewMetaListVo> metaList) {
        ApmSpaceAppService service = PlmContextHolder.getBean(ApmSpaceAppService.class);
        return new MappingContext<>(keys ->
                service.list(service.lambdaQuery()
                        .select(ApmSpaceApp::getBid, ApmSpaceApp::getName)
                        .getWrapper()
                        .eq(ApmSpaceApp::getDeleteFlag, 0)
                        .in(ApmSpaceApp::getBid, keys)
                ).stream().collect(Collectors.toMap(ApmSpaceApp::getBid, ApmSpaceApp::getName, (m1, m2) -> m1))
                , ViewMetaListVo::getSpaceAppBid, false, metaList);
    }

}
