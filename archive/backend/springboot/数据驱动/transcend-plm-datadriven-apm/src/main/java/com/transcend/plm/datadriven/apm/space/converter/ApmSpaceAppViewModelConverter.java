package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmSpaceAppViewModelConverter {
    ApmSpaceAppViewModelConverter INSTANCE = Mappers.getMapper(ApmSpaceAppViewModelConverter.class);

    /**
     * po2vo
     *
     * @param apmSpaceAppViewModelPo apmSpaceAppViewModelPo
     * @return AppViewModelDto
     */
    AppViewModelDto po2vo(ApmSpaceAppViewModelPo apmSpaceAppViewModelPo);

    /**
     * pos2vos
     *
     * @param apmSpaceAppViewModelPos apmSpaceAppViewModelPos
     * @return List<AppViewModelDto>
     */
    List<AppViewModelDto> pos2vos(List<ApmSpaceAppViewModelPo> apmSpaceAppViewModelPos);

    /**
     * vos2pos
     *
     * @param appViewModelDtos appViewModelDtos
     * @return List<ApmSpaceAppViewModelPo>
     */
    List<ApmSpaceAppViewModelPo> vos2pos(List<AppViewModelDto> appViewModelDtos);

    ApmSpaceAppViewModelPo dto2po(AppViewModelDto appViewModelDto);
}
