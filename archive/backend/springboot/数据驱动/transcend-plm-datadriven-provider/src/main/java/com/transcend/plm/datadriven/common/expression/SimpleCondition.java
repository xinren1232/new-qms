package com.transcend.plm.datadriven.common.expression;


import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 判断类型
 * 表示一组条件判断逻辑
 * 每个枚举常量实现了一个条件逻辑，接受两个对象进行评估
 * 还包含一个中文名称字段
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/20 17:47
 */
public enum SimpleCondition {
    /**
     * 等于条件
     */
    @SuppressWarnings("all")
    EQ("等于", (left, right) -> {
        if (left instanceof Collection && right instanceof Collection) {
            ArrayList<?> leftCopy = new ArrayList<>((Collection<?>) left);
            leftCopy.removeAll((Collection<?>) right);
            return leftCopy.isEmpty();
        }
        //由于页面配置的条件值始终是字符串，需要处理属性类型是integer的情况
        if (left != null && left instanceof Integer) {
            return ((Integer) left).toString().equals(right);
        }
        return Objects.equals(left, right);
    }),

    /**
     * 不等于条件
     */
    NE("不等于", (left, right) -> !Objects.equals(left, right)),

    /**
     * 包含于条件
     */
    IN("包含于", (left, right) -> right instanceof Collection && ((Collection<?>) right).contains(left)),

    /**
     * 不包含于条件
     */
    NOT_IN("不包含于", (left, right) -> right instanceof Collection && !((Collection<?>) right).contains(left)),

    /**
     * 为空条件
     */
    NULL("为空", (left, right) -> left == null),

    /**
     * 不为空条件
     */
    NOT_NULL("不为空", (left, right) -> left != null),

    /**
     * 大于条件
     */
    GT("大于", (left, right) -> left != null && right != null && compare(left, right) > 0),

    /**
     * 小于条件
     */
    LT("小于", (left, right) -> left != null && right != null && compare(left, right) < 0),

    /**
     * 大于或等于条件
     */
    GE("大于或等于", (left, right) -> left != null && right != null && compare(left, right) >= 0),

    /**
     * 小于或等于条件
     */
    LE("小于或等于", (left, right) -> left != null && right != null && compare(left, right) <= 0);

    @Getter
    private final String name;
    private final BiFunction<Object, Object, Boolean> function;

    /**
     * 构造函数，初始化每个枚举常量的中文名称和逻辑
     *
     * @param name     中文名称
     * @param function 实现条件逻辑的 BiFunction
     */
    @SuppressWarnings("all")
    SimpleCondition(String name, BiFunction<Object, Object, Boolean> function) {
        this.name = name;
        this.function = function;
    }

    /**
     * 比较两个对象
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 返回比较结果：负数表示小于，0表示等于，正数表示大于
     * @throws IllegalArgumentException 如果对象不可比较
     */
    @SuppressWarnings("unchecked")
    private static int compare(Object left, Object right) {
        if (left instanceof Comparable && right instanceof Comparable) {
            return ((Comparable<Object>) left).compareTo(right);
        }
        throw new IllegalArgumentException("Objects are not comparable");
    }

    /**
     * 评估条件逻辑
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 返回条件判断的结果
     */
    public boolean evaluate(Object left, Object right) {
        return function.apply(left, right);
    }


}
