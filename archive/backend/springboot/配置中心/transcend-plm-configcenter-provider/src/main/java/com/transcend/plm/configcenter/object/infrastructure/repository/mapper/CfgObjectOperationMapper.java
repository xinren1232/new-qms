package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectOperationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface CfgObjectOperationMapper {

    /**
     * 根据基类模型列出操作
     * @param baseModel 对象類型
     * @return List<CfgObjectPermissionOperationPo>
     */
    List<CfgObjectOperationPo> listByBaseModel(@Param("baseModel") String baseModel);
    /**
     * 查询默认以及对象类型的按钮
     * @param baseModel 对象類型
     * @return List<CfgObjectPermissionOperationPo>
     */
    List<CfgObjectOperationPo> listByBaseModelAndDefalut(@Param("baseModel") String baseModel);

    /**
     *
     * @param baseModel 对象類型
     * @return Set<String>
     */
    Set<String> findCodeByBaseModelAndDefalut(@Param("baseModel") String baseModel);


    /**
     *
     * @param baseModel 对象類型
     * @return Set<String>
     */
    Set<String> findCodeByBaseModel(@Param("baseModel") String baseModel);

    /**
     * 查询所有
     * @return List<AuthOperationPO>
     */
    List<CfgObjectOperationPo> findAll();

    int copyOperations(@Param("sourceBaseModel") String sourceBaseModel, @Param("targetBaseModel") String baseModel);

}
