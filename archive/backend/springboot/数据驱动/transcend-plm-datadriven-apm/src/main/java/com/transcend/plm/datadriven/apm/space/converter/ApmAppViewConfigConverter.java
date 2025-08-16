package com.transcend.plm.datadriven.apm.space.converter;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppViewConfigDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppViewConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface ApmAppViewConfigConverter {
    ApmAppViewConfigConverter INSTANCE = Mappers.getMapper(ApmAppViewConfigConverter.class);
    /**
     * 将ApmAppViewConfigDto对象转换为ApmAppViewConfig对象。
     *
     * @param dto 待转换的ApmAppViewConfigDto对象
     * @return 转换后的ApmAppViewConfig对象
     */
    ApmAppViewConfig dto2po(ApmAppViewConfigDto dto);

    /**
     * 将ApmAppViewConfig对象列表转换为ApmAppViewConfigVo对象列表。
     *
     * @param pos 待转换的ApmAppViewConfig对象列表
     * @return 转换后的ApmAppViewConfigVo对象列表
     */
    List<ApmAppViewConfigVo> pos2vos(List<ApmAppViewConfig> pos);

    /**
     * 将ApmAppViewConfig对象转换为ApmAppViewConfigVo对象。
     *
     * @param po 待转换的ApmAppViewConfig对象
     * @return 转换后的ApmAppViewConfigVo对象
     */
    ApmAppViewConfigVo po2vo(ApmAppViewConfig po);

    /**
     * 将ApmAppViewConfig对象转换为ApmAppViewConfig对象。
     *
     * @param po 待转换的ApmAppViewConfig对象
     * @return 转换后的ApmAppViewConfig对象
     */
    ApmAppViewConfig po2po(ApmAppViewConfig po);
}
