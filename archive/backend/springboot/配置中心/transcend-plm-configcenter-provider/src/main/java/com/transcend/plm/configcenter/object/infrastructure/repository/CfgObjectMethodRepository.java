package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.transcend.plm.configcenter.object.infrastructure.po.ModelMethodPO;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectModelMethodMapper;
import com.transcend.plm.configcenter.api.model.object.qo.ModelMethodQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 10:19
 */
@Repository
@Mapper
public class CfgObjectMethodRepository {
    @Resource
    private ObjectModelMethodMapper objectModelMethodMapper;

    public List<ModelMethodPO> query(ModelMethodQO methodQO) {
        return objectModelMethodMapper.query(methodQO);
    }

    public Boolean bulkInsert(List<ModelMethodPO> modelMetodPo) {
        return objectModelMethodMapper.bulkInsert(modelMetodPo);
    }

    public Boolean bulkUpdate(List<ModelMethodPO> modelMetodPo) {
        return objectModelMethodMapper.bulkUpdate(modelMetodPo);
    }

    public Boolean deleteByBid(String bid) {
        return objectModelMethodMapper.deleteByBid(bid);
    }

    public ModelMethodPO findByBid(String bid) {
        return objectModelMethodMapper.findByBid(bid);
    }

    public ModelMethodPO findName(String methodName) {
        return objectModelMethodMapper.findName(methodName);
    }
}