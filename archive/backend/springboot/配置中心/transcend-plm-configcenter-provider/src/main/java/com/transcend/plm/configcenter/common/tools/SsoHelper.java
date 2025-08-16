//package com.transcend.plm.configcenter.common.tools;
//
//import com.transsion.framework.auth.IUser;
//import com.transsion.framework.auth.IUserContext;
//import com.transcend.framework.sso.tool.SsoHelper;
//
///**
// * SSO获取用户信息
// *
// * @author jie.luo <jie.luo1@transsion.com>
// * @version V1.0.0
// * @date 2023/5/11 19:51
// * @since 1.0
// */
//public class SsoHelper {
//    public static String getJobNumber(){
//        IUserContext<IUser> user = UserContextHolder.getUser();
//        if (user == null){
//            return "";
//        }
//        return user.getCode();
//    }
//
//    public static String getName(){
//        IUserContext<IUser> user = UserContextHolder.getUser();
//        if (user == null){
//            return "";
//        }
//        return user.getName();
//    }
//}
