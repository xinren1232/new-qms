package com.transcend.plm.configcenter.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.space.repository.po.ApmSpaceApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApmSpaceAppMapper extends BaseMapper<ApmSpaceApp> {

    List<ApmSpaceApp> getByMc(@Param("modelCode") String modelCode);

    List<ApmSpaceApp> getByMcs(@Param("modelCodes") List<String> modelCodes);
}




