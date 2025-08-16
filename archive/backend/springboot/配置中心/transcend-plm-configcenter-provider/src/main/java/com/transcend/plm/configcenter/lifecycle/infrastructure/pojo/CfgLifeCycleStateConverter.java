package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleStatePo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author yuanhu.huang
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgLifeCycleStateConverter {

    CfgLifeCycleStateConverter INSTANCE = Mappers.getMapper(CfgLifeCycleStateConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgLifeCycleStatePo dto2po(LifeCycleStateDto dto);

    /**
     * do 转化为 vo
     * @return
     */
    LifeCycleStateVo po2vo(CfgLifeCycleStatePo cfgLifeCycleStatePo);


    /**
     * do 转化为 vo
     * @param cfgLifeCycleStatePos
     * @return
     */
    List<LifeCycleStateVo> pos2vos(List<CfgLifeCycleStatePo> cfgLifeCycleStatePos);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgLifeCycleStatePo> dtos2pos(List<LifeCycleStateDto> cfgAttributeDtos);
}
