package com.transcend.plm.configcenter.common.exception;

import com.transsion.framework.enums.BaseEnum;
import com.transsion.framework.exception.BusinessException;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-04-03 14:28
 **/
public class PlmDataException extends BusinessException {

    public PlmDataException(BaseEnum<?> errorEnum) {
        super(String.valueOf(errorEnum.getCode()),errorEnum.getDesc());
    }

    public PlmDataException(String message) {
        super(message);
    }

    public PlmDataException(String code, String message) {
        super(code,message);
    }

    public PlmDataException(String message, Throwable cause) {
        super(message,cause);
    }

    public PlmBizExceptionBuilder builder() {
        return new PlmBizExceptionBuilder();
    }

    public static class PlmBizExceptionBuilder {
        String code;
        String message;
        Throwable cause;
        BaseEnum<?> errorEnum;
        public PlmBizExceptionBuilder code(String code) {
            this.code = code;
            return this;
        }

        public PlmBizExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        public PlmBizExceptionBuilder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }
        public PlmBizExceptionBuilder errorEnum(BaseEnum<?> errorEnum) {
            this.errorEnum = errorEnum;
            return this;
        }

        public PlmDataException build() {
            String errCode;
            String errMsg;
            if (errorEnum != null) {
                errCode = String.valueOf(errorEnum.getCode());
                errMsg = errorEnum.getDesc();
            } else {
                errCode = code;
                errMsg = message;
            }
            PlmDataException plmBizException;
            if (cause != null) {
                plmBizException = new PlmDataException(errMsg, cause);
                plmBizException.setCode(errCode);
            } else {
                plmBizException = new PlmDataException(errCode, errMsg);
            }
            return plmBizException;
        }
    }


}
