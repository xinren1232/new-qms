package com.transcend.plm.datadriven.common.util;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;

/**
 * @author unknown
 */
@Slf4j
public class ObjectCode4Utils {

    public static final int MODEL_CODE_SIZE_NUM = 4;
    private final static String RESET_FIRST_NUM_CHAR = "0";
    private final static String RESET_FIRST_UPPERCASE_CHAR = "A";
    private final static String EXT_CHAR = "$";
    /**
     * 正则匹配 只能是大写和数字 以及$
     */
    private final static String REGEX = "[A-Z0-9$]+";
    /**
     * 限制层数
     */
    private static final int LEVEL_LIMIT = 10;

    /**
     * 此处生成会存在并发问题，多台服务器，此处加锁也不能解决（此处业务会串行执行，因此可忽略） TODO
     *
     * @param code
     * @return null 则生成失败
     */
    public static String increaseCode(String code) {
        log.info("需要递增的编码：{}", code);
        // 1.校验非空
        if (StringUtils.isEmpty(code)) {
            return "0000";
        }
        // 2.校验是否符合
        checkMatches(code);
        // 3.切割level1[4],level2[4]...
        int level = getLevel(code);
        checkLevelLimit(level);
        int length = code.length();
        // 后缀code,需要递增的code
        String preMaxCode = code.substring(0, length - MODEL_CODE_SIZE_NUM);
        String suffixMaxCode = code.substring(length - MODEL_CODE_SIZE_NUM, length);
        // 第一层级有三位，因此需要特殊处理，第二层级开始递增即可
        String firstCode = suffixMaxCode.substring(0, 1);
        String secondCode = suffixMaxCode.substring(1, 2);
        String thirdCode = suffixMaxCode.substring(2,3);
        String fourCode = suffixMaxCode.substring(3,4);
        String genCode = increaseAsciiCode(fourCode);
        // “第四位未满”的情况，第四位+1
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + firstCode + secondCode + thirdCode + genCode;
        }
        // “第四位满+第三位未满”的情况，第四位重置，第三位+1
        genCode = increaseAsciiCode(thirdCode);
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + firstCode + secondCode + genCode + RESET_FIRST_NUM_CHAR;
        }
        // “第三位满+第二位未满”的情况，第三、四位重置，第二位+1
        genCode = increaseAsciiCode(secondCode);
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + firstCode + genCode + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR;
        }
        // “第二位满+第一位未满”的情况，第二、三、四位重置，第一位+1
        genCode = increaseAsciiCode(firstCode);
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + genCode + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR;
        }
        // 第一位满的情况（非扩展特殊字符）允许使用扩展
        if (!EXT_CHAR.equals(firstCode)) {
            return preMaxCode + EXT_CHAR + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR;
        }
        log.error("第" + level + "层，已经超字符！整个字符为：{}", code);
        throw new RuntimeException("第"+level+"层，已经超字符！");

    }

    /**
     * @param level
     */
    private static void checkLevelLimit(int level) {
        if (level > LEVEL_LIMIT){
            log.error("限制10层！当前整个字符层数为：{}", level);
            throw new RuntimeException("第"+level+"层，已经超字符！");
        }
    }

    public static void checkLevelLimit(String parentCode) {
        int level = getLevel(parentCode);
        if (level > LEVEL_LIMIT){
            log.error("限制10层！当前整个字符层数为：{}", level);
            throw new RuntimeException("第"+level+"层，已经超字符！");
        }

    }

    public static String increaseNextLevelCode(String maxCode) {
        return maxCode + RESET_FIRST_NUM_CHAR;
    }

    public static String increaseAsciiCode(String levelCode) {
        char c = levelCode.charAt(0);
        int asciiCode = (int) c;
        // ascii 数字 0123456789 范围
        if (asciiCode >= 48 && asciiCode < 57) {
            return Character.toString((char) ++asciiCode);
        }
        if (asciiCode == 57) {
            return RESET_FIRST_UPPERCASE_CHAR;
        }
        // ascii 大写 0123456789 范围
        if (asciiCode >= 65 && asciiCode < 90) {
            return Character.toString((char) ++asciiCode);
        }
        return null;
    }


    public static String getInitCode() {
        return "0000";
    }

    /**
     * 判断层级
     */
    public static int getLevel(String code) {
        int length = code.length();
        if (length % MODEL_CODE_SIZE_NUM != 0) {
            log.error("不是正常编码规则，四的倍数");
            throw new RuntimeException("不是正常编码规则，四的倍数");
        }
        return length / 4;
    }

    /**
     * 收集模型code所有包括继承的父级
     * @param code 00000A00W->000/00000A/00000A/00000A00W
     * @return >000/00000A/00000A/00000A00W
     */
    public static LinkedHashSet<String> splitModelCode(String code){
        if (StringUtils.isBlank(code)){
            return Sets.newLinkedHashSet();
        }
        // 校验
        checkMatches(code);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        int level = getLevel(code);
        for (int i = 0; i < level; i++) {
            String substring = code.substring(0, (i + 1) * MODEL_CODE_SIZE_NUM);
            result.add(substring);
        }
        return result;
    }

    private static void checkMatches(String modelCode) {
        // 正则匹配 只能是大写和数字 以及$
        if(!modelCode.matches(REGEX)){
            log.error("不是正常编码规则，正则匹配 只能是大写和数字 以及$，输入参数为：【{}】", modelCode);
            throw new RuntimeException("正则匹配 只能是大写和数字 以及$");
        }
    }

    /**
     * 收集模型code所有包括继承的父级 - 从小到大
     * @param modelCode
     * @return 0000000A0010->0000000A0010/0000000A/0000
     */
    public static LinkedHashSet<String> splitModelCodeDesc(String modelCode){
        if (StringUtils.isBlank(modelCode)){
            return Sets.newLinkedHashSet();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        int level = getLevel(modelCode);
        for (int i = level; i > 0; i--) {
            String substring = modelCode.substring(0, (i) * MODEL_CODE_SIZE_NUM);
            result.add(substring);
        }
        return result;
    }

//    public static void main(String[] args) {
//        LinkedHashSet<String> strings = ObjectCode4Utils.splitModelCode("0000000A0010");
//        System.out.printf(JSON.toJSONString(strings));
//        String code = "0ZZ$22ZA";
//        for (int i = 0; i < 500; i++) {
//            code = increaseCode(code);
//            System.out.println(i + "----" + code);
//        }
//    }
}
