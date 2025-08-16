package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppExportTemplateDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppExportTemplateVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppExportTemplatePo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * ApmAppExportTemplateConverter接口定义了一些转换方法，用于将ApmAppExportTemplatePo对象转换为ApmAppExportTemplateVo对象，以及将列表中的对象进行转换。
 *
 * 方法列表：
 *
 * - ApmAppExportTemplateVo po2vo(ApmAppExportTemplatePo po)
 *     - 将ApmAppExportTemplatePo对象转换为ApmAppExportTemplateVo对象
 *
 * - List<ApmAppExportTemplateVo> pos2vos(List<ApmAppExportTemplatePo> pos)
 *     - 将ApmAppExportTemplatePo对象列表转换为ApmAppExportTemplateVo对象列表
 *
 * - ApmAppExportTemplatePo dto2po(ApmAppExportTemplateDto dto)
 *     - 将ApmAppExportTemplateDto对象转换为ApmAppExportTemplatePo对象
 *
 * - List<ApmAppExportTemplatePo> dto2pos(List<ApmAppExportTemplateDto> dtos)
 *     - 将ApmAppExportTemplateDto对象列表转换为ApmAppExportTemplatePo对象列表
 * @author unknown
 */
@Mapper
public interface ApmAppExportTemplateConverter {

    ApmAppExportTemplateConverter INSTANCE = Mappers.getMapper(ApmAppExportTemplateConverter.class);

    /**
     *
     * 将ApmAppExportTemplatePo对象转换为ApmAppExportTemplateVo对象。
     *
     * @param po ApmAppExportTemplatePo对象
     * @return ApmAppExportTemplateVo对象
     */
    ApmAppExportTemplateVo po2vo(ApmAppExportTemplatePo po);

    /**
     *
     * 将ApmAppExportTemplatePo对象列表转换为ApmAppExportTemplateVo对象列表。
     *
     * @param pos ApmAppExportTemplatePo对象列表
     * @return ApmAppExportTemplateVo对象列表
     */
    List<ApmAppExportTemplateVo> pos2vos(List<ApmAppExportTemplatePo> pos);

    /**
     * 将ApmAppExportTemplateDto对象转换为ApmAppExportTemplatePo对象。
     *
     * @param dto ApmAppExportTemplateDto对象
     * @return ApmAppExportTemplatePo对象
     */
    ApmAppExportTemplatePo dto2po(ApmAppExportTemplateDto dto);

    /**
     * 将ApmAppExportTemplateDto对象列表转换为ApmAppExportTemplatePo对象列表。
     *
     * @param dtos ApmAppExportTemplateDto对象列表
     * @return ApmAppExportTemplatePo对象列表
     */
    List<ApmAppExportTemplatePo> dto2pos(List<ApmAppExportTemplateDto> dtos);
}
