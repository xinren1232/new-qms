package com.transcend.plm.configcenter.common.annotation;

import java.lang.annotation.*;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-27 11:41
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckTableDataExist {
    String tableName();
    String fieldName();
    boolean exist();
}
