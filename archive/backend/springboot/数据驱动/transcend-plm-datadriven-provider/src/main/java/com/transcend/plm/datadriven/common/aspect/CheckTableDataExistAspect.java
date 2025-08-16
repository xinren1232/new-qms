package com.transcend.plm.datadriven.common.aspect;

import com.transcend.plm.datadriven.common.annotation.CheckTableDataExist;
import com.transcend.plm.datadriven.common.dao.ConfigCommonMapper;
import com.transcend.plm.datadriven.common.validator.UniqueValidateParam;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Aspect
@Slf4j
@Component
public class CheckTableDataExistAspect {

    @Resource
    private ConfigCommonMapper configCommonMapper;

    /**
     *
     */
    @Pointcut("@annotation(com.transcend.plm.datadriven.common.annotation.CheckTableDataExist)")
    public void checkTableDataExistPointcut() {
        // Do nothing because of pointcut define
    }

    /**
     * @param joinPoint
     */
    @Before("checkTableDataExistPointcut()")
    public void checkTableDataExist(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        //获取注解中的tableName
        CheckTableDataExist annotation = method.getAnnotation(CheckTableDataExist.class);
        String tableName = annotation.tableName();
        Assert.hasText(tableName, "tableName is not found in annotation");
        String fieldName = annotation.fieldName();
        Assert.hasText(tableName, "fieldName is not found in annotation");
        //获取方法中参数bid的值
        String[] parameterNames = signature.getParameterNames();
        int index = ArrayUtils.indexOf(parameterNames, StringUtil.underlineToCamel(fieldName));
        if (index == -1) {
            throw new IllegalStateException("parameter "+StringUtil.underlineToCamel(fieldName)+" is not found");
        }
        Object[] args = joinPoint.getArgs();
        int count = configCommonMapper.countByField(UniqueValidateParam.builder().tableName(tableName)
                .columnName(fieldName).value(args[index])
                .excludeLogicDeleteItems(Boolean.TRUE).build()
        );
        if ((count > 0) != annotation.exist()) {
            String statement = count > 1 ? "exist" : "not exist";
            throw new IllegalStateException("data " + statement);
        }
    }
}
