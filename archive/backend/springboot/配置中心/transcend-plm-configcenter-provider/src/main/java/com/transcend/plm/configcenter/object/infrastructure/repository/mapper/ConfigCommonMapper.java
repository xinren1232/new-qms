package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.common.validator.UniqueValidateParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-27 13:48
 **/
@Mapper
public interface ConfigCommonMapper {
    /**
     * 根据bid汇总
     * @param tableName
     * @param filedName
     * @param conditionValue
     * @return
     */
    int countByCondition(String tableName, String filedName,String conditionValue);

    /**
     * 查询满足条件的数据行数
     * @param param
     * @return
     */
    int countByField(@Param("param") UniqueValidateParam param);
}
