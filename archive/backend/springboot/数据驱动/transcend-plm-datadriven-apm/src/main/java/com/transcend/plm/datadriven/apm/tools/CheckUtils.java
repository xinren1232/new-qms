package com.transcend.plm.datadriven.apm.tools;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * description: 检查字符是否满足条件(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/14 9:37
 * @version V1.0
 */
public class CheckUtils {

    /**
     * 检查条件是否满足
     * @param checkValue 检查值
     * @param relationship 关系
     * @param compareValue 比较值
     * @return
     */
    public static boolean checkConditionBoolean(Object checkValue, String relationship, Object compareValue) {
        if (relationship == null || relationship.length() == 0) {
            return false;
        }
        // 统一转为大写
        relationship = relationship.toUpperCase();
        switch (relationship) {
            // 等于
            case "EQ":
            case "EQUAL":
                return checkEq(checkValue, compareValue);
            // 不等于
            case "NE":
            case "NOTEQUAL":
                return checkNe(checkValue, compareValue);
            // 大于
            case "GT":
                return checkGt(checkValue, compareValue);
            // 小于
            case "LT":
                return checkLt(checkValue, compareValue);
            // 大于等于
            case "GE":
            case "NOTLT":
                return checkGe(checkValue, compareValue);
            // 小于等于
            case "LE":
            case "NOTGT":
                return checkLe(checkValue, compareValue);
            // 为空
            case "NULL":
            case "IS_NULL":
                return checkIsNull(checkValue);
            // 不为空
            case "NOTNULL":
            case "IS_NOT_NULL":
                return checkIsNotNull(checkValue);
            // 包含
            case "LIKE":
            case "CONTAIN":
            case "IN":
                return checkContain(checkValue, compareValue);
            // 不包含
            case "NOTCONTAIN":
            case "NOT IN":
                return checkNotContain(checkValue, compareValue);
            default:
                return false;

        }
    }

    /**
     * 检查是否不包含
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkNotContain(Object checkValue, Object compareValue) {
        return !checkContain(checkValue, compareValue);
    }

    /**
     * 检查是否包含
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkContain(Object checkValue, Object compareValue) {
        if (checkValue == null || compareValue == null) {
            return false;
        }
        if (checkValue instanceof String && compareValue instanceof String) {
            return ((String) compareValue).contains((String) checkValue);
        }
        if (compareValue instanceof Collection) {
            return ((Collection<T>) compareValue).contains((T) checkValue);
        }
        List<String> list = getObjectList(compareValue);
        if (list != null) {
            return list.contains((String) checkValue);
        }
        return false;
    }

    /**
     * 检查是否不为空
     * @param checkValue
     * @return
     */
    private static boolean checkIsNotNull(Object checkValue) {
        if (checkValue instanceof Collection) {
            return CollectionUtils.isEmpty((Collection<?>) checkValue);
        }
        if (checkValue instanceof String) {
            return StringUtils.isEmpty((String) checkValue);
        }
        if (checkValue != null) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为空
     * @param checkValue
     * @return
     */
    private static boolean checkIsNull(Object checkValue) {
        if (checkValue == null) {
            return true;
        }
        if (checkValue instanceof Collection) {
            return CollectionUtils.isEmpty((Collection<?>) checkValue);
        }
        if (checkValue instanceof String) {
            return StringUtils.isEmpty((String) checkValue);
        }
        return false;
    }

    /**
     * 判断是否小于等于
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkLe(Object checkValue, Object compareValue) {
        if (checkValue == null || compareValue == null) {
            return false;
        }
        if (checkValue instanceof Number && compareValue instanceof Number) {
            return ((Number)checkValue).doubleValue() - ((Number)compareValue).doubleValue() <= 1e-10;
        }
        if (getObjectDate(checkValue) != null && getObjectDate(compareValue) != null) {
            return getObjectDate(checkValue).before(getObjectDate(compareValue)) || getObjectDate(checkValue).equals(getObjectDate(compareValue));
        }
        return false;
    }

    /**
     * 判断是否大于等于
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkGe(Object checkValue, Object compareValue) {
        if (checkValue == null || compareValue == null) {
            return false;
        }
        if (checkValue instanceof Number && compareValue instanceof Number) {
            return ((Number)checkValue).doubleValue() - ((Number)compareValue).doubleValue() >= 1e-10;
        }
        if (getObjectDate(checkValue) != null && getObjectDate(compareValue) != null) {
            return getObjectDate(checkValue).after(getObjectDate(compareValue)) || getObjectDate(checkValue).equals(getObjectDate(compareValue));
        }
        return false;
    }

    /**
     * 检查是否小于
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkLt(Object checkValue, Object compareValue) {
        if (checkValue == null || compareValue == null) {
            return false;
        }
        if (checkValue instanceof Number && compareValue instanceof Number) {
            return ((Number)checkValue).doubleValue() - ((Number)compareValue).doubleValue() < 1e-10;
        }
        if (getObjectDate(checkValue) != null && getObjectDate(compareValue) != null) {
            return getObjectDate(checkValue).before(getObjectDate(compareValue));
        }
        return false;
    }

    /**
     * 检查是否大于
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkGt(Object checkValue, Object compareValue) {
        if (checkValue == null || compareValue == null) {
            return false;
        }
        if (checkValue instanceof Number && compareValue instanceof Number) {
            return ((Number)checkValue).doubleValue() - ((Number)compareValue).doubleValue() > 1e-10;
        }
        if (getObjectDate(checkValue) != null && getObjectDate(compareValue) != null) {
            return getObjectDate(checkValue).after(getObjectDate(compareValue));
        }
        return false;
    }

    /**
     * 检查是否不相等
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkNe(Object checkValue, Object compareValue) {
        return !checkEq(checkValue, compareValue);
    }

    /**
     * 检查是否相等
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkEq(Object checkValue, Object compareValue) {
        if (checkValue == null && compareValue == null) {
            return true;
        }
        if ((checkValue == null && compareValue != null) || (checkValue != null && compareValue == null)) {
            return false;
        }
        return checkValue.equals(compareValue);
    }

    /**
     * 检查类型是否一致
     * @param checkValue
     * @param compareValue
     * @return
     */
    private static boolean checkType(Object checkValue, Object compareValue) {
        // 使用反射来获取对象的类型
        Class<?> type1 = checkValue.getClass();
        Class<?> type2 = compareValue.getClass();
        return type1.equals(type2);
    }


    /**
     * object 转为List<String>
     * @param object
     * @return
     */
    private static List<String> getObjectList(Object object){
        List<String> list = new ArrayList<>();
        if(object == null){
            return list;
        }
        if(object instanceof List){
            list = JSON.parseArray(object.toString(), String.class);
        }else if(object instanceof String){
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if(StringUtils.isNotEmpty(objectStr)){
                list.add(objectStr);
            }
        }else{
            if(object.toString().startsWith(CommonConstant.OPEN_BRACKET)){
                list = JSON.parseArray(object.toString(), String.class);
            }else{
                list.add(object.toString());
            }
        }
        return list;
    }

    private static Date getObjectDate(Object object){
        if(object instanceof Date){
            return (Date)object;
        }else if (object instanceof LocalDateTime){
            LocalDateTime localDateTime = (LocalDateTime)object;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        else{
            try {
                if(object.toString().length() == DatePattern.NORM_DATETIME_PATTERN.length()){
                    return   DateUtil.parseDateTime(object.toString());
                }else if (object.toString().length() == DatePattern.NORM_DATE_PATTERN.length()){
                    return  DateUtil.parseDate(object.toString());
                }
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
//
//
//
//
//
//
//
//    if(CollectionUtils.isEmpty(apmFlowNodeDirectionConditionList)){
//        return true;
//    }
//        for(
//    ApmFlowNodeDirectionCondition apmFlowNodeDirectionCondition:apmFlowNodeDirectionConditionList){
//        /**
//         * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于，between.在区间
//         */
//        String relationship = apmFlowNodeDirectionCondition.getRelationship();
//        String filedName = apmFlowNodeDirectionCondition.getFiledName();
//        String filedValue = apmFlowNodeDirectionCondition.getFiledValue();
//        /**
//         * 条件比较值类型，string.字符串，number.数字，date.日期，role.角色，now.日期当前时间。loginUser.当前登录人工号
//         */
//        String filedValueType = apmFlowNodeDirectionCondition.getFiledValueType();
//
//        Object value = mSpaceAppData.get(filedName);
//        boolean result = false;
//        //空判断
//        if("null".equals(relationship)){
//            if(value == null){
//                result = true;
//            }else{
//                List<String> valueList = getObjectList(value);
//                if(CollectionUtils.isEmpty(valueList)){
//                    result = true;
//                }
//            }
//        }
//        //非空判断
//        if("notNull".equals(relationship)){
//            if(value != null){
//                List<String> valueList = getObjectList(value);
//                if(CollectionUtils.isNotEmpty(valueList)){
//                    result = true;
//                }
//            }
//        }
//        //包含
//        if("contain".equals(relationship)){
//            if(value != null && value.toString().contains(filedValue)){
//                result = true;
//            }
//        }
//        //不包含
//        if("notContain".equals(relationship)){
//            if(value != null && !value.toString().contains(filedValue)){
//                result = true;
//            }
//        }
//        //等于
//        if("equal".equals(relationship)){
//            if(value != null && value.toString().equals(filedValue)){
//                result = true;
//            }
//        }
//        //不等于
//        if("notEqual".equals(relationship)){
//            if(value != null && !value.toString().equals(filedValue)){
//                result = true;
//            }
//        }
//        //gt.大于，
//        if("gt".equals(relationship) && value != null){
//            if("number".equals(filedValueType)){
//                //数字类型判断
//                Double valueDouble = Double.valueOf(value.toString());
//                Double filedValueDouble = Double.valueOf(filedValue);
//                if(valueDouble > filedValueDouble){
//                    result = true;
//                }
//            }else if("date".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = DateUtil.parseDateTime(filedValue);
//                if(valueDate.getTime() > filedValueDate.getTime()){
//                    result = true;
//                }
//            }else if("now".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = new Date();
//                if(valueDate.getTime() > filedValueDate.getTime()){
//                    result = true;
//                }
//            } else {
//                if (value != null && value.toString().compareTo(filedValue) > 0) {
//                    result = true;
//                }
//            }
//        }
//        //lt.小于，
//        if("lt".equals(relationship) && value != null){
//            if("number".equals(filedValueType)){
//                //数字类型判断
//                Double valueDouble = Double.valueOf(value.toString());
//                Double filedValueDouble = Double.valueOf(filedValue);
//                if(valueDouble < filedValueDouble){
//                    result = true;
//                }
//            }else if("date".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = DateUtil.parseDateTime(filedValue);
//                if(valueDate.getTime() < filedValueDate.getTime()){
//                    result = true;
//                }
//            }else if("now".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = new Date();
//                if(valueDate.getTime() < filedValueDate.getTime()){
//                    result = true;
//                }
//            } else {
//                if(value != null && value.toString().compareTo(filedValue) < 0){
//                    result = true;
//                }
//            }
//        }
//        //notGt.小于等于，
//        if("notGt".equals(relationship) && value != null){
//            if("number".equals(filedValueType)){
//                //数字类型判断
//                Double valueDouble = Double.valueOf(value.toString());
//                Double filedValueDouble = Double.valueOf(filedValue);
//                if(valueDouble <= filedValueDouble){
//                    result = true;
//                }
//            }else if("date".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = DateUtil.parseDateTime(filedValue);
//                if(valueDate.getTime() <= filedValueDate.getTime()){
//                    result = true;
//                }
//            }else if("now".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = new Date();
//                if(valueDate.getTime() <= filedValueDate.getTime()){
//                    result = true;
//                }
//            } else {
//                if (value != null && value.toString().compareTo(filedValue) <= 0) {
//                    result = true;
//                }
//            }
//        }
//        //notLt.大于等于
//        if("notLt".equals(relationship)){
//            if("number".equals(filedValueType)){
//                //数字类型判断
//                Double valueDouble = Double.valueOf(value.toString());
//                Double filedValueDouble = Double.valueOf(filedValue);
//                if(valueDouble >= filedValueDouble){
//                    result = true;
//                }
//            }else if("date".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = DateUtil.parseDateTime(filedValue);
//                if(valueDate.getTime() >= filedValueDate.getTime()){
//                    result = true;
//                }
//            }else if("now".equals(filedValueType)){
//                //日期类型
//                Date valueDate = getObjectDate(value);
//                Date filedValueDate = new Date();
//                if(valueDate.getTime() >= filedValueDate.getTime()){
//                    result = true;
//                }
//            } else {
//                if (value != null && value.toString().compareTo(filedValue) >= 0) {
//                    result = true;
//                }
//            }
//        }
//        if(FlowEnum.FLOW_MATCH_ALL.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch()) && !result){
//            return false;
//        }
//        if(FlowEnum.FLOW_MATCH_ANY.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch()) && result){
//            return true;
//        }
//    }
//        if(FlowEnum.FLOW_MATCH_ALL.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch())){
//        return true;
//    }
//        if(FlowEnum.FLOW_MATCH_ANY.getCode().equals(apmFlowNodeDirectionVO.getDirectionMatch())){
//        return false;
//    }
//        return false;
}
