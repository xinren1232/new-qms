package com.transcend.plm.datadriven.common.dao;


import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.validator.UniqueValidateParam;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.exception.BusinessException;
import org.springframework.util.ObjectUtils;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023年3月22日11:35:43
 **/
public class TableUniqueValueService {

    private TableUniqueValueService(){}

    /**
     * 判断表中某个字段是否已经存在该值
     *
     */
    public static boolean getFiledUniqueFlag(UniqueValidateParam uniqueValidateParam) {
        try {
            return doValidate(uniqueValidateParam);
        } catch (Exception exception) {
            throw new BusinessException(exception.getMessage());
        }
    }

    private static boolean doValidate(UniqueValidateParam uniqueValidateParam) {
        ConfigCommonMapper commonMapper = PlmContextHolder.getBean(ConfigCommonMapper.class);
        // 参数校验
        paramValidate(uniqueValidateParam);

        // 排除当前记录，排除逻辑删除的内容
        Boolean excludeCurrentRecord = uniqueValidateParam.getExcludeCurrentRecord();
        if (Boolean.TRUE.equals(excludeCurrentRecord)) {
            // id判空
            paramIdValidate(uniqueValidateParam);
        }

        // 如果大于0，代表不是唯一的当前校验的值
        return commonMapper.countByField(uniqueValidateParam) <= 0;
    }


    /**
     * 几个参数的为空校验
     *
     */
    private static void paramValidate(com.transcend.plm.datadriven.common.validator.UniqueValidateParam uniqueValidateParam) {
        if (StringUtil.isBlank(uniqueValidateParam.getTableName())) {
            throw new BusinessException("@TableUniqueValue注解上tableName属性为空");
        }
        if (StringUtil.isBlank(uniqueValidateParam.getColumnName())) {
            throw new BusinessException( "@TableUniqueValue注解上columnName属性为空");
        }
        if (ObjectUtils.isEmpty(uniqueValidateParam.getValue())) {
            throw new BusinessException("@TableUniqueValue被校验属性的值为空");
        }
    }

    /**
     * id参数的为空校验
     *
     */
    private static void paramIdValidate(com.transcend.plm.datadriven.common.validator.UniqueValidateParam uniqueValidateParam) {
        if (uniqueValidateParam.getId() == null) {
            throw new BusinessException(StringUtil.underlineToCamel(uniqueValidateParam.getIdFieldName()) + "参数值为空");
        }
    }

}
