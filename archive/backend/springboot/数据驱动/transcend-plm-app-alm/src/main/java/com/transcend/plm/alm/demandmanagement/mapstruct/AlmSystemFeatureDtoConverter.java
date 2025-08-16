package com.transcend.plm.alm.demandmanagement.mapstruct;

import cn.hutool.core.convert.Convert;
import com.transcend.plm.alm.demandmanagement.entity.wrapper.SystemFeatureWrapper;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.event.handler.SystemFeatureScmpUpdateEventHandler;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.common.mapping.MappingContext;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 特性树传输对象转换类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/6 10:14
 */
@Mapper
public interface AlmSystemFeatureDtoConverter {
    AlmSystemFeatureDtoConverter INSTANCE = Mappers.getMapper(AlmSystemFeatureDtoConverter.class);

    /**
     * 拼接多个值
     * 使用英文逗号对多个值进行拼接
     *
     * @param object 需要处理的对象
     * @return 拼接过后的值
     */
    default String stitchMultipleValues(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Collection) {
            return ((Collection<?>) object).stream()
                    .filter(o -> o instanceof String)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else if (object.getClass().isArray()) {
            return Arrays.stream((Object[]) object)
                    .filter(o -> o instanceof String)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            return String.valueOf(object);
        }
    }

    /**
     * 转换数据传输对象
     *
     * @param belongDomainNameContext 所属域名称映射上下文
     * @param object                  需要转换的对象
     * @return 转换后的对象
     */
    default AlmSystemFeatureDTO toDto(MappingContext<String, String, String> belongDomainNameContext,
                                      Map<String, Object> object) {
        if (object == null) {
            return null;
        }
        SystemFeatureWrapper wrapper = new SystemFeatureWrapper(object);
        AlmSystemFeatureDTO dto = new AlmSystemFeatureDTO();
        dto.setBid(wrapper.getBid());
        dto.setCoding(wrapper.getCoding());
        dto.setName(wrapper.getName());
        dto.setLevel(wrapper.getLevel());
        dto.setFeatureSe(stitchMultipleValues(wrapper.getFeatureSe()));
        dto.setParentBid(wrapper.getParentBid());
        dto.setSpaceBid(wrapper.getSpaceBid());
        dto.setSpaceAppBid(wrapper.getSpaceAppBid());
        dto.setFeatureOwner(stitchMultipleValues(wrapper.getFeatureOwner()));
        dto.setDescription(wrapper.getDemandDesc());

        dto.setBelongDomain(getBelongDomainNames(belongDomainNameContext, wrapper.getBelongDomain()));

        return dto;
    }

    /**
     * 转换数据传输对象
     *
     * @param object 需要转换的对象
     * @return 转换后的对象
     */
    default AlmSystemFeatureDTO toDto(Map<String, Object> object) {
        return toDto(getBelongDomainNameContext(true), object);
    }


    /**
     * 转换为数据传输对象列表
     *
     * @param list 需要转换的对象列表
     * @return 转换后的对象列表
     */
    default List<AlmSystemFeatureDTO> toDtoList(List<MObject> list) {
        if (list == null) {
            return null;
        }
        MappingContext<String, String, String> belongDomainNameContext = getBelongDomainNameContext(false);

        List<String> belongDomainBidList = list.stream().map(SystemFeatureWrapper::new).map(SystemFeatureWrapper::getBelongDomain)
                .map(object -> Convert.toList(String.class, object)).filter(Objects::nonNull)
                .flatMap(Collection::stream).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        belongDomainNameContext.loadInstance(belongDomainBidList);

        return list.stream().map(o -> toDto(belongDomainNameContext, o)).collect(Collectors.toList());
    }


    /**
     * 转换为请求SCMP参数对象
     *
     * @param belongDomainNameContext 所属域名称映射上下文
     * @param object                  需要转换的对象
     * @return 请求参数对象
     */
    default SystemFeatureScmpUpdateEventHandler.Params toParams(MappingContext<String, String, String> belongDomainNameContext,
                                                                Map<String, Object> object) {
        if (object == null) {
            return null;
        }
        SystemFeatureWrapper wrapper = new SystemFeatureWrapper(object);
        SystemFeatureScmpUpdateEventHandler.Params params = new SystemFeatureScmpUpdateEventHandler.Params();
        params.setFeatureBid(wrapper.getBid());
        params.setFeatureCode(wrapper.getCoding());
        params.setFeatureName(wrapper.getName());
        params.setLevel(wrapper.getLevel());
        params.setFeatureSe(stitchMultipleValues(wrapper.getFeatureSe()));
        params.setSpaceBid(wrapper.getSpaceBid());
        params.setSpaceAppBid(wrapper.getSpaceAppBid());
        params.setFeatureOwner(stitchMultipleValues(wrapper.getFeatureOwner()));
        params.setFeatureDesc(wrapper.getDemandDesc());

        params.setBelongDomain(getBelongDomainNames(belongDomainNameContext, wrapper.getBelongDomain()));

        return params;
    }

    /**
     * 获取所属域名称
     *
     * @param belongDomainNameContext 映射上下文
     * @param object                  需要转换的对象
     * @return 所属域名称
     */
    default String getBelongDomainNames(MappingContext<String, String, String> belongDomainNameContext, Object object) {
        List<String> belongDomain = Convert.toList(String.class, object);
        belongDomain = belongDomain.stream().filter(StringUtils::isNotBlank)
                .map(bid -> belongDomainNameContext.getOrDefault(bid, bid)).collect(Collectors.toList());
        if (belongDomain.isEmpty()) {
            return null;
        }
        return stitchMultipleValues(belongDomain);
    }


    /**
     * 转换为请求SCMP参数对象
     *
     * @param object 需要转换的对象
     * @return 请求参数对象
     */
    default SystemFeatureScmpUpdateEventHandler.Params toParams(Map<String, Object> object) {
        return toParams(getBelongDomainNameContext(true), object);
    }

    /**
     * 获取所属域名称映射上下文
     *
     * @param forceMapped 是否强制映射
     * @return 映射上下文
     */
    default MappingContext<String, String, String> getBelongDomainNameContext(boolean forceMapped) {
        return new MappingContext<>(keys -> {
            ObjectModelStandardI<?> objectModelCrudI = PlmContextHolder.getBean(ObjectModelStandardI.class);
            QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getCode(), keys);
            return objectModelCrudI.list(
                    TranscendModel.DOMAIN.getCode(), QueryWrapper.buildSqlQo(wrapper)
            ).stream().collect(Collectors.toMap(MBaseData::getBid, MObject::getName));
        }, Function.identity(), forceMapped);
    }

}
