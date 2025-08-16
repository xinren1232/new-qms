package com.transcend.plm.alm.tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * description: 工具类(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/25 8:55
 * @version V1.0
 */
public class TranscendTools {

    /**
     * 树扁平化： 如果树中包含嵌套的子树，使用集合将它们扁平化。
     *
     * @param root
     * @param childrenGetter
     * @return
     */
    public static <T> List<T> flattenTreeIterative(List<T> root, Function<T, List<T>> childrenGetter) {
        List<T> flattenedList = new ArrayList<>();
        if (root == null) {
            return flattenedList;
        }
        Deque<T> stack = new ArrayDeque<>();
        for (T selectVo : root) {
            stack.push(selectVo);
        }
        while (!stack.isEmpty()) {
            T node = stack.pop();
            flattenedList.add(node);
            List<T> children = childrenGetter.apply(node);
            if (children != null) {
                // 将子节点逆序入栈，以保持前序遍历的顺序
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
        }

        return flattenedList;
    }

    /**
     * 树扁平化： 如果树中包含嵌套的子树，使用递归将它们扁平化。
     *
     * @param list
     * @param childrenGetter
     * @param <T>
     * @return
     */
    public static <T> List<T> flattenNameList(List<T> list, Function<T, List<T>> childrenGetter) {
        List<T> flattenedList = new ArrayList<>();
        for (T selectVo : list) {
            addSelectVoAndChildren(selectVo, flattenedList, childrenGetter);
        }
        return flattenedList;
    }


    /**
     * 递归地将SelectVo及其子元素添加到扁平化列表中
     */
    private static <T> void addSelectVoAndChildren(T selectVo, List<T> list, Function<T, List<T>> childrenGetter) {
        if (selectVo == null) {
            return;
        }
        // 添加当前SelectVo的名称
        list.add(selectVo);
        if (childrenGetter.apply(selectVo) != null) {
            for (T child : childrenGetter.apply(selectVo)) {
                // 递归处理子元素
                addSelectVoAndChildren(child, list, childrenGetter);
            }
        }
    }


}
