package com.transcend.plm.configcenter.common.expression;

import lombok.Data;

import java.util.List;

/**
 * 简单控制表达式
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/20 18:33
 */
@Data
public class SimpleExpression {
    /**
     * 数据库数据
     */
    private final Object left;
    /**
     * 条件
     */
    private final SimpleCondition condition;
    /**
     * 匹配参数
     */
    private final Object right;


    /**
     * 评估当前条件表达式
     *
     * @return 如果条件表达式匹配成功，返回true；否则返回false
     */
    public boolean evaluate() {
        return this.condition.evaluate(left, right);
    }


    /**
     * 评估多个条件表达式
     *
     * @param expressions 条件表达式列表
     * @param anyMatch    是否需要任意条件匹配
     * @return 如果模式是任意匹配，且至少一个表达式匹配成功，返回true；
     * 如果模式是全部匹配，且所有表达式匹配成功，返回true；
     * 否则返回false
     */
    public static boolean evaluateExpressions(List<SimpleExpression> expressions, boolean anyMatch) {
        for (SimpleExpression expression : expressions) {
            boolean result = expression.getCondition().evaluate(expression.getLeft(), expression.getRight());
            if (anyMatch && result) {
                // 任意匹配模式中，有一个匹配则返回true
                return true;
            }
            if (!anyMatch && !result) {
                // 全部匹配模式中，有一个不匹配则返回false
                return false;
            }
        }
        // 如果是全部匹配模式且没有不匹配的，返回true；否则返回false
        return !anyMatch;
    }

    /**
     * 构建条件表达式
     *
     * @param left      数据库数据
     * @param condition 条件
     * @param right     匹配参数
     * @return 条件表达式
     */
    public static SimpleExpression of(Object left, String condition, Object right) {
        return new SimpleExpression(left, SimpleCondition.valueOf(condition), right);
    }

}
