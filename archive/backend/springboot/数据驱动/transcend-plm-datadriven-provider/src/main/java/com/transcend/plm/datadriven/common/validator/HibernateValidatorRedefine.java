// package com.transcend.plm.datadriven.common.validator;

// import net.bytebuddy.ByteBuddy;
// import net.bytebuddy.agent.ByteBuddyAgent;
// import net.bytebuddy.asm.Advice;
// import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
// import net.bytebuddy.matcher.ElementMatchers;
// import org.hibernate.validator.internal.engine.ValidatorImpl;
// import org.springframework.boot.autoconfigure.AutoConfigureBefore;
// import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
// import org.springframework.context.annotation.Configuration;

// /**
//  * @Program transcend-plm-datadriven
//  * @Description 重新定义validator的validateParameters方法
//  * @Author peng.qin
//  * @Version 1.0
//  * @Date 2023-03-24 16:49
//  **/
// @Configuration
// @AutoConfigureBefore(ValidationAutoConfiguration.class)
// public class HibernateValidatorRedefine {
//     public HibernateValidatorRedefine(){}
//     static{
//         ByteBuddyAgent.install();
//         ClassReloadingStrategy classReloadingStrategy = new ClassReloadingStrategy(ByteBuddyAgent.getInstrumentation(), ClassReloadingStrategy.Strategy.REDEFINITION);
//         new ByteBuddy()
//                 .redefine(ValidatorImpl.class)
//                 .visit(Advice.to(ValidatorImplAdvice.class)
//                         .on(ElementMatchers.named("validateParameters").and(ElementMatchers.takesArguments(4))))
//                 .make()
//                 .load(ValidatorImpl.class.getClassLoader(), classReloadingStrategy);
//     }
// }
