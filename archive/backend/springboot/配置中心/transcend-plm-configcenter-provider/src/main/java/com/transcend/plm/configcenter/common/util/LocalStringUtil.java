package com.transcend.plm.configcenter.common.util;

public class LocalStringUtil {
    public static boolean checkCode(String code){
        char[] chars = code.toCharArray();
        if(chars.length>50){
            return false;
        }
        for(int i=0;i<chars.length;i++){
            if(i == 0){
                //首字母必须是小写字母
                //判断字符是否为小写字母
                if(!Character.isLowerCase(chars[i])){
                    return false;
                }
            }else{
                if(!Character.isLetter(chars[i]) && !Character.isDigit(chars[i])){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断是否是大写
     * @param code
     * @return
     */
    public static boolean checkUpperCode(String code){
        char[] chars = code.toCharArray();
        for(int i=0;i<chars.length;i++){
            if(!Character.isUpperCase(chars[i])){
                return false;
            }
        }
        return true;
    }
}
