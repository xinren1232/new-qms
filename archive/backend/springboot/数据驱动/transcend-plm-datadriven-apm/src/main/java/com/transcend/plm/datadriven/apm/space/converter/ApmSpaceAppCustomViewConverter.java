package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppCustomViewDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppCustomViewVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewPo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmSpaceAppCustomViewConverter {
    ApmSpaceAppCustomViewConverter INSTANCE = Mappers.getMapper(ApmSpaceAppCustomViewConverter.class);

    /**
     * 方法描述
     *
     * @param apmSpaceAppCustomViewPos apmSpaceAppCustomViewPos
     * @return 返回值
     */
    List<ApmSpaceAppCustomViewVo> pos2vos(List<ApmSpaceAppCustomViewPo> apmSpaceAppCustomViewPos);

    /**
     * 方法描述
     *
     * @param po po
     * @return 返回值
     */
    ApmSpaceAppCustomViewVo po2vo(ApmSpaceAppCustomViewPo po);

    /**
     * 方法描述
     *
     * @param apmSpaceAppCustomViewDto apmSpaceAppCustomViewDto
     * @return 返回值
     */
    ApmSpaceAppCustomViewPo dto2po(ApmSpaceAppCustomViewDto apmSpaceAppCustomViewDto);
}
