package com.transcend.plm.datadriven.common.validator;

import net.bytebuddy.asm.Advice;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-24 16:41
 **/
public class ValidatorImplAdvice {
    private ValidatorImplAdvice(){}
    @Advice.OnMethodEnter
    public static void initGroupContext(@Advice.AllArguments Object[] args){
        Class<?>[] groups = (Class<?>[])args[args.length-1];
        if (groups.length > 0 && groups[0] instanceof Class) {

            // 临时保存group的class值
            ValidatorGroupContext.set(groups[0]);
        }
    }

    @Advice.OnMethodExit
    public static void clearGroupContext(@Advice.AllArguments Object[] args){
        ValidatorGroupContext.clear();
    }
}
