package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmSpaceConverter {
    ApmSpaceConverter INSTANCE = Mappers.getMapper(ApmSpaceConverter.class);

    /**
     * po2vo
     *
     * @param apmSpace apmSpace
     * @return ApmSpaceVo
     */
    ApmSpaceVo po2vo(ApmSpace apmSpace);

    /**
     * pos2vos
     *
     * @param apmSpaces apmSpaces
     * @return List<ApmSpaceVo>
     */
    List<ApmSpaceVo> pos2vos(List<ApmSpace> apmSpaces);

    /**
     * qo2po
     *
     * @param param param
     * @return ApmSpace
     */
    ApmSpace qo2po(ApmSpaceQo param);
}
