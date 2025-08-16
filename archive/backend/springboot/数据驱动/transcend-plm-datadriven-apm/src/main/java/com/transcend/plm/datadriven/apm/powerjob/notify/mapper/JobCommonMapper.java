package com.transcend.plm.datadriven.apm.powerjob.notify.mapper;


import org.apache.ibatis.annotations.Mapper;

/**
 * @author yuanhu.huang
 * @version 1.0
 * @Program transcend-plm-datadriven
 * 基础数据驱动Mapper
 * @date 2023-02-22 10:12
 **/
@Mapper
public interface JobCommonMapper<T> {

    void deletePersonmgrBakData();

    void insertPersonmgrBakData();

    void deletePersonmgrData();

    void insertPersonmgrData();

    void insertPiProjectRoleMonth();

    void deletePiProjectRoleMonth();
}
