package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.Page;
import com.transcend.plm.configcenter.object.infrastructure.po.ObjectRelationPO;
import com.transcend.plm.configcenter.api.model.object.qo.ObjectRelationQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaonan.xu
 * @version: 1.0
 * @date 2021/8/31 9:36
 */
@Mapper
@Repository
public interface ObjectRelationModelMapper {

    //新增
    Integer insert(@Param("relationList") List<ObjectRelationPO> relationList);

    //编辑
    Integer edit(@Param("relationList") List<ObjectRelationPO> relationList);

    //查询
    List<ObjectRelationPO> find(@Param("qo") ObjectRelationQO qo);

    /**
     * 根据源对象id列表查询关系->对象继承
     * @param sourceModelCodes
     * @return
     */
    List<ObjectRelationPO> findBySourceModelCodes(List<String> sourceModelCodes);

    //分页查询
    IPage<ObjectRelationPO> query(Page<ObjectRelationPO> page, @Param("qo") ObjectRelationQO qo);
    //分页查询
    List<ObjectRelationPO> query(@Param("qo") ObjectRelationQO qo);

    //通过源对象和关联对象查询
    List<ObjectRelationPO> findByrelationFlagect(@Param("relationList") List<ObjectRelationPO> relationList);

    //删除
    Integer delete(@Param("relationList") List<ObjectRelationPO> relationList);
}
