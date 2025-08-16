package com.transcend.plm.configcenter.common.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transsion.framework.common.ObjectUtil;
import com.transsion.framework.tool.SpringBeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;

@Slf4j
public class ObjectCodeUtils {


    public static final int MODEL_CODE_SIZE_NUM = 3;
    private final static String RESET_FIRST_NUM_CHAR = "0";
    private final static String RESET_FIRST_UPPERCASE_CHAR = "A";

    /**
     * 限制层数
     */
    private static final int LEVEL_LIMIT = 10;
    /**
     * 此处生成会存在并发问题，多台服务器，此处加锁也不能解决（此处业务会串行执行，因此可忽略）
     *
     * @param maxCode
     * @return null 则生成失败
     */
    public static String increaseCode(String maxCode) {
        log.info("需要递增的编码：{}", maxCode);
        // 1.校验非空
        if (StringUtils.isEmpty(maxCode)) {
            return "000";
        }
        // 2.校验是否符合 A a 2 TODO
        // 3.切割level1[3],level2[3]...
        int level = getLevel(maxCode);
        int length = maxCode.length();
        // 后缀code,需要递增的code
        String preMaxCode = maxCode.substring(0, length - 3);
        String suffixMaxCode = maxCode.substring(length - 3, length);
        // 第一层级有三位，因此需要特殊处理，第二层级开始递增即可
        String firstCode = suffixMaxCode.substring(0, 1);
        String secondCode = suffixMaxCode.substring(1, 2);
        String thirdCode = suffixMaxCode.substring(2,3);
        String genCode = increaseAsciiCode(thirdCode);
        // 不为空的场景为已经用完字符
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + firstCode + secondCode + genCode;
        }
        // 第三位满的情况，第三位重置，第二位+1
        genCode = increaseAsciiCode(secondCode);
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + firstCode + genCode + RESET_FIRST_NUM_CHAR;
        }
        // 第二位满的情况，第二位重置，第一位+1
        genCode = increaseAsciiCode(secondCode);
        if (StringUtils.isNotBlank(genCode)) {
            return preMaxCode + genCode + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR;
        }
        // 第一位满的情况，报错
        genCode = increaseAsciiCode(firstCode);
        if (StringUtils.isBlank(genCode)) {
            return preMaxCode + genCode + RESET_FIRST_NUM_CHAR + RESET_FIRST_NUM_CHAR;
        }
        log.error("第" + level + "层，已经超字符！整个字符为：{}", maxCode);
        throw new RuntimeException("第"+level+"层，已经超字符！");

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
        /*if(asciiCode == 90){
            return RESET_FIRST_LOWERCASE_CHAR;
        }
        // ascii 小写 0123456789 范围
        if (asciiCode>=97 && asciiCode<122){
            return Character.toString((char) ++asciiCode);
        }
        if(asciiCode == 122){
            return null;
        }*/
        return null;
    }

    /**
     * 判断层级
     */
    public static int getLevel(String code) {
        if(ObjectTypeEnum.getByCode(code) != null || CommonConst.ROLE_TREE_DEFAULT_ROOT_BID.equals(code)){
            return 1;
        }
        int length = code.length();
        if (length % 3 != 0) {
            log.error("不是正常编码规则，三的倍数");
            throw new RuntimeException("不是正常编码规则，三的倍数");
        }
        return length / 3;
    }

    /**
     * 收集模型code所有包括继承的父级
     * @param modelCode 00000A00W->000/00000A/00000A/00000A00W
     * @return >000/00000A/00000A/00000A00W
     */
    public static LinkedHashSet<String> splitModelCode(String modelCode){
        if (StringUtils.isBlank(modelCode)){
            return Sets.newLinkedHashSet();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        int level = getLevel(modelCode);
        for (int i = 0; i < level; i++) {
            String substring = modelCode.substring(0, (i + 1) * 3);
            result.add(substring);
        }
        return result;
    }

    /**
     * 收集模型code所有包括继承的父级 - 从小到大
     * @param modelCode
     * @return 00000A00W->00000A00W/00000A/000
     */
    public static LinkedHashSet<String> splitModelCodeDesc(String modelCode){
        if (StringUtils.isBlank(modelCode)){
            return Sets.newLinkedHashSet();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        //传入基类时，直接返回
        if(ObjectTypeEnum.ROOT.getCode().equals(modelCode)){
            result.add(modelCode);
            return result;
        }
        boolean isBootObject = ObjectTypeEnum.NORMAL.getCode().equals(modelCode) || ObjectTypeEnum.VERSION.getCode().equals(modelCode) || ObjectTypeEnum.RELATION.getCode().equals(modelCode);
        if (isBootObject) {
            result.add(modelCode);
            result.add(ObjectTypeEnum.ROOT.getCode());
            return result;
        }

        //进行拆分
        int level = getLevel(modelCode);
        for (int i = level; i > 0; i--) {
            String substring = modelCode.substring(0, (i) * 3);
            result.add(substring);
        }
        //增加根类型
        ICfgObjectAppService cfgObjectAppService = SpringBeanHelper.getBean(ICfgObjectAppService.class);
        CfgObjectVo objectVo = cfgObjectAppService.getByModelCode(modelCode);
        Assert.notNull(objectVo, "模型不存在");
        Assert.hasText(objectVo.getType(), "对象类型为空");
        result.add(objectVo.getType());
        result.add(ObjectTypeEnum.ROOT.getCode());
        return result;
    }

    public static String getInitCode() {
        return "A00";
    }


    public static void checkLevelLimit(String parentCode) {
        int level = getLevel(parentCode);
        if (level > LEVEL_LIMIT){
            log.error("限制10层！当前整个字符层数为：{}", level);
            throw new RuntimeException("第"+level+"层，已经超字符！");
        }

    }

    public static Boolean isBaseModel(String modelCode) {
        return ObjectUtil.equals(modelCode.length(), ObjectCodeUtils.MODEL_CODE_SIZE_NUM);
    }

    public static String getBaseModelCode(String modelCode) {
        return modelCode.substring(0, MODEL_CODE_SIZE_NUM);
    }

    public static void main(String[] args) {
        LinkedHashSet<String> strings = ObjectCodeUtils.splitModelCode("00D00A00W");
        System.out.printf(JSON.toJSONString(strings));
        String code = "000000";
//        for (int i = 0; i < 500; i++) {
//            code = IpmCodeUtil.increaseCode(code);
//            System.out.println(i + "----" + code);
//        }
    }

}
