package com.transcend.plm.datadriven.common.web.exception;

import com.transsion.framework.enums.BaseEnum;
import com.transsion.framework.exception.BusinessException;

/**
 * 异常构造器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2020/11/28 12:38
 * @since 1.0
 */
public class ExceptionConstructor {
    public static RuntimeException runtimeBusExpConstructor(String code, String msg) {
        throw new BusinessException(code, msg);
    }
    public static RuntimeException runtimeBusExpConstructor(BaseEnum<String> resultEnum) {
        throw new BusinessException(resultEnum.getCode(), resultEnum.getDesc());
    }
    private ExceptionConstructor(){}
}
