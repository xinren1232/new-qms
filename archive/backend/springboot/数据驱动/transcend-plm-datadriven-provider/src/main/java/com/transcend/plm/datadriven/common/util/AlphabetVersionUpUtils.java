package com.transcend.plm.datadriven.common.util;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.transsion.framework.exception.BusinessException;

import java.util.HashMap;

/**
 * 大写字母版本提升获取类
 * @author leigang.yang
 * @version: 1.0
 * @date 2021/06/07 12:07
 */
public class AlphabetVersionUpUtils {

    private AlphabetVersionUpUtils(){
    }

    private static HashMap<String, String> reviseMap = new HashMap();

    private static final String UP = "Z";

    private static final String START = "A";

    static {
        reviseMap.put("A", "B");
        reviseMap.put("B", "C");
        reviseMap.put("C", "D");
        reviseMap.put("D", "E");
        reviseMap.put("E", "F");
        reviseMap.put("F", "G");
        reviseMap.put("G", "H");
        reviseMap.put("H", "I");
        reviseMap.put("I", "J");
        reviseMap.put("J", "K");
        reviseMap.put("K", "L");
        reviseMap.put("L", "M");
        reviseMap.put("M", "N");
        reviseMap.put("N", "O");
        reviseMap.put("O", "P");
        reviseMap.put("P", "Q");
        reviseMap.put("Q", "R");
        reviseMap.put("R", "S");
        reviseMap.put("S", "T");
        reviseMap.put("T", "U");
        reviseMap.put("U", "V");
        reviseMap.put("V", "W");
        reviseMap.put("W", "X");
        reviseMap.put("X", "Y");
        reviseMap.put("Y", "Z");
        reviseMap.put("Z", "A");
    }

    public static String getNext(String str) {
        if (StringUtils.isBlank(str)){
            throw new BusinessException("数据有误");
        }
        return next(str, str.length());
    }

    private static String next(String str, int i){
        int j = i - 1;
        if (j < 0) {
            return str + START;
        }

        String flag = str.substring(j, i);
        String next = reviseMap.get(flag);
        if (StringUtils.isBlank(next)){
            throw new BusinessException("存在非法字符！");
        }

        str = str.substring(0, j) + next + str.substring(i);

        if (flag.equals(UP)) {
            str = next(str, j);
        }
        return str;
    }

//    public static void main(String[] args) {
//        String str = "AZ";
//        String str1 = "AE 2";
//        String str2 = "";
//        String str3 = "ZZZ";
//        System.out.println(getNext(str));
//    }
}
