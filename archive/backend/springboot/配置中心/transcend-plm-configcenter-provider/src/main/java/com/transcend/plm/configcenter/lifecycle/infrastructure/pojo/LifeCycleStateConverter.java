package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.LifeCycleStatePo;
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
public interface LifeCycleStateConverter {

    LifeCycleStateConverter INSTANCE = Mappers.getMapper(LifeCycleStateConverter.class);
    /**
     * do 转化为 vo
     * @param LifeCycleStatePos
     * @return
     */
    List<LifeCycleStateVo> pos2vos(List<LifeCycleStatePo> LifeCycleStatePos);


}
