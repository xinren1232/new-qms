package com.transcend.plm.datadriven.apm.tools;

import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.tabmanage.enums.ViewComponentEnum;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视图元数据工具
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/29 17:54
 */
@NoArgsConstructor
public class ViewMetaUtils {

    /**
     * 补偿视图元数据
     *
     * @param modelCode 模型编码
     * @param metaList  元数据列表
     * @return 补偿后的元数据列表
     */
    public static List<CfgViewMetaDto> compensationMetadata(String modelCode, List<CfgViewMetaDto> metaList) {
        if (StringUtils.isBlank(modelCode)) {
            return metaList;
        }
        metaList = Optional.ofNullable(metaList).orElseGet(ArrayList::new);

        List<CfgObjectAttributeVo> attributeList = PlmContextHolder.getBean(CfgObjectFeignClient.class)
                .listObjectAttribute(modelCode).getCheckExceptionData();
        Set<String> codeNameList = metaList.stream().map(CfgViewMetaDto::getName).collect(Collectors.toSet());
        Optional.ofNullable(attributeList)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().filter(attr -> !codeNameList.contains(attr.getCode()))
                        .map(attr -> CfgViewMetaDto.builder().name(attr.getCode()).label(attr.getName())
                                .type(ViewComponentEnum.INPUT_CONSTANT).build()).collect(Collectors.toList())
                ).filter(list -> !list.isEmpty()).ifPresent(metaList::addAll);
        return metaList;
    }


}
