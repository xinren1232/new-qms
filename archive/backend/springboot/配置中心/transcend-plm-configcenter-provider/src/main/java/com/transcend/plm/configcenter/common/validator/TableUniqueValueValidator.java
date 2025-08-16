package com.transcend.plm.configcenter.common.validator;

import cn.hutool.core.lang.Dict;
import com.transcend.plm.configcenter.common.annotation.RequestAnnotation;
import com.transcend.plm.configcenter.common.annotation.TableUniqueValue;
import com.transcend.plm.configcenter.common.dao.TableUniqueValueService;
import com.transsion.framework.common.StringUtil;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Program transcend-plm-configcenter
 * @Description 验证表中是否存在唯一值
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
public class TableUniqueValueValidator implements ConstraintValidator<TableUniqueValue, Object> {

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * id字段的名称
     */
    private String idFieldName;

    /**
     * 是否开启逻辑删除校验，默认是关闭的
     * <p>
     * 关于为何开启逻辑删除校验：
     * <p>
     * 若项目中某个表包含控制逻辑删除的字段，我们在进行唯一值校验的时候要排除这种状态的记录，所以需要用到这个功能
     */
    private boolean excludeLogicDeleteItems;

    /**
     * 逻辑删除的字段名称
     */
    private String logicDeleteFieldName;

    /**
     * 默认逻辑删除的值（Y是已删除）
     */
    private boolean logicDeleteValue;

    @Override
    public void initialize(TableUniqueValue constraintAnnotation) {
        this.tableName = constraintAnnotation.tableName();
        this.columnName = constraintAnnotation.columnName();
        this.excludeLogicDeleteItems = constraintAnnotation.excludeLogicDeleteItems();
        this.logicDeleteFieldName = constraintAnnotation.logicDeleteFieldName();
        this.logicDeleteValue = constraintAnnotation.logicDeleteValue();
        this.idFieldName = constraintAnnotation.idFieldName();
    }

    @Override
    public boolean isValid(Object fieldValue, ConstraintValidatorContext context) {

        if (ObjectUtils.isEmpty(fieldValue)) {
            return true;
        }

        Class<?> validateGroupClass = ValidatorGroupContext.get();
        //save and update 合并在一起时，通过id值进行区分
        String bid = RequestParamContext.get().getStr("bid");
        boolean edit = RequestAnnotation.edit.class.equals(validateGroupClass) || (validateGroupClass == null && StringUtil.isNotBlank(bid));
        // 如果属于edit group，校验时需要排除当前修改的这条记录
        if (edit) {
            UniqueValidateParam editParam = createEditParam(fieldValue);
            return TableUniqueValueService.getFiledUniqueFlag(editParam);
        }

        // 如果属于add group，则校验库中所有行
        if (RequestAnnotation.add.class.equals(validateGroupClass)) {
            UniqueValidateParam addParam = createAddParam(fieldValue);
            return TableUniqueValueService.getFiledUniqueFlag(addParam);
        }

        // 默认校验所有的行
        UniqueValidateParam addParam = createAddParam(fieldValue);
        return TableUniqueValueService.getFiledUniqueFlag(addParam);
    }

    /**
     * 创建校验新增的参数
     *
     */
    private UniqueValidateParam createAddParam(Object fieldValue) {
        return UniqueValidateParam.builder()
                .tableName(tableName)
                .columnName(columnName)
                .value(fieldValue)
                .excludeCurrentRecord(Boolean.FALSE)
                .excludeLogicDeleteItems(excludeLogicDeleteItems)
                .logicDeleteFieldName(logicDeleteFieldName)
                .logicDeleteValue(logicDeleteValue).build();
    }

    /**
     * 创建修改的参数校验
     *
     */
    private UniqueValidateParam createEditParam(Object fieldValue) {

        // 获取请求字段中id的值
        Dict requestParam = RequestParamContext.get();

        // 获取id字段的驼峰命名法
        String camelCaseIdFieldName = StringUtil.underlineToCamel(idFieldName);

        return UniqueValidateParam.builder()
                .tableName(tableName)
                .columnName(columnName)
                .value(fieldValue)
                .idFieldName(idFieldName)
                .excludeCurrentRecord(Boolean.TRUE)
                .id(requestParam.getStr(camelCaseIdFieldName))
                .excludeLogicDeleteItems(excludeLogicDeleteItems)
                .logicDeleteFieldName(logicDeleteFieldName)
                .logicDeleteValue(logicDeleteValue).build();
    }

}
