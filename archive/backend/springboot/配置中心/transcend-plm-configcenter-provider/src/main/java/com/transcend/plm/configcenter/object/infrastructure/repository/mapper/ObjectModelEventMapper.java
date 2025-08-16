package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.configcenter.object.infrastructure.po.ModelEventPO;
import com.transcend.plm.configcenter.api.model.object.qo.ModelEventQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelEventVO;
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
public interface ObjectModelEventMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ModelEventPO record);

    int insertSelective(ModelEventPO record);

    ModelEventPO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ModelEventPO record);

    int updateByPrimaryKey(ModelEventPO record);

    IPage<ModelEventPO> query(Page<ModelEventVO> page, @Param("qo") ModelEventQO modelEventQO);

    Boolean bulkInsert(@Param("records") List<ModelEventPO> modelEventPOS);
}