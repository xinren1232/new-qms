package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.common.dao.MyBatisBaseDao;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.LifeCycleStatePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LifeCycleStateMapper extends MyBatisBaseDao<LifeCycleStatePo, Integer> {
    List<LifeCycleStatePo> list(@Param("lifeCycleStatusListQo") LifeCycleStateListQo lifeCycleStatusListQo);
    int bulkInsert(@Param("records") List<LifeCycleStatePo> records);
    List<LifeCycleStatePo> queryByCodes(@Param("list") List<String> list);

    List<LifeCycleStatePo> allList();

}
