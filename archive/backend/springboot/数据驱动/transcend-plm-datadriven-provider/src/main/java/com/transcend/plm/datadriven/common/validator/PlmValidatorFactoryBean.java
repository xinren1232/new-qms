package com.transcend.plm.datadriven.common.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
@Slf4j
public class PlmValidatorFactoryBean extends LocalValidatorFactoryBean {

    /**
     * @param target
     * @param errors
     * @param validationHints
     */
    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {

        try {
            if (validationHints.length > 0 && validationHints[0] instanceof Class) {
                // 临时保存group的class值
                ValidatorGroupContext.set((Class<?>) validationHints[0]);
            }
            super.validate(target, errors, validationHints);
        } finally {
            ValidatorGroupContext.clear();
        }
    }

}
