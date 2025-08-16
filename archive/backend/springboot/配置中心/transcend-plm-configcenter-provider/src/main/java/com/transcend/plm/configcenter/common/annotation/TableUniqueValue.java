package com.transcend.plm.configcenter.common.annotation;

import com.transcend.plm.configcenter.common.validator.TableUniqueValueValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
@Documented
@Constraint(validatedBy = TableUniqueValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableUniqueValue {

    String message() default "库中存在重复编码，请更换该编码值";

    Class[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 表名称，例如 sys_user
     */
    String tableName();

    /**
     * 列名称，例如 user_code
     */
    String columnName();

    /**
     * 数据库主键id的字段名，例如 user_id
     */
    String idFieldName() default "bid";

    /**
     * 是否开启逻辑删除校验，默认是关闭的
     * <p>
     * 关于为何开启逻辑删除校验：
     * <p>
     * 若项目中某个表包含控制逻辑删除的字段，我们在进行唯一值校验的时候要排除这种状态的记录，所以需要用到这个功能
     */
    boolean excludeLogicDeleteItems() default true;

    /**
     * 逻辑删除的字段名称
     */
    String logicDeleteFieldName() default "delete_flag";

    /**
     * 默认逻辑删除的值（false是未删除）
     */
    boolean logicDeleteValue() default false;

    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        TableUniqueValue[] value();
    }
}
