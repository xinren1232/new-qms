package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.ModelMethodPO;
import com.transcend.plm.configcenter.api.model.object.qo.ModelMethodQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 10:19
 */
@Repository
@Mapper
public interface ObjectModelMethodMapper {
    List<ModelMethodPO> query(@Param("qo") ModelMethodQO methodQO);

    Boolean bulkInsert(@Param("records") List<ModelMethodPO> modelMetodPo);

    Boolean bulkUpdate(@Param("records") List<ModelMethodPO> modelMetodPo);

    Boolean deleteByBid(String bid);

    ModelMethodPO findByBid(String bid);

    ModelMethodPO findName(String methodName);
}