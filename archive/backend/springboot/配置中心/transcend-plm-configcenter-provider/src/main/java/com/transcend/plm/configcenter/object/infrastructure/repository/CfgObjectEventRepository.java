package com.transcend.plm.configcenter.object.infrastructure.repository;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.configcenter.object.infrastructure.po.ModelEventPO;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectModelEventMapper;
import com.transcend.plm.configcenter.api.model.object.qo.ModelEventQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelEventVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 10:19
 */
@Repository
@Mapper
public class CfgObjectEventRepository {
    @Resource
    private ObjectModelEventMapper objectModelEventMapper;

    public int insert(ModelEventPO record){
        return objectModelEventMapper.insert(record);
    }


    public IPage<ModelEventPO> query(Page<ModelEventVO> page, @Param("qo") ModelEventQO modelEventQO){
        return objectModelEventMapper.query(page, modelEventQO);
    }

}