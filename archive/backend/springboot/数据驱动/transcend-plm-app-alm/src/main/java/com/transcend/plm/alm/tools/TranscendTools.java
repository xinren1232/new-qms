package com.transcend.plm.alm.tools;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;

/**
 * description: 工具类(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/25 8:55
 * @version V1.0
 */
public class TranscendTools {

    public static List<ApmFlowInstanceRoleUser> getUsers(List<String> users, String rrBid, String roleBid, String handleFlag, String spaceAppBid, String spaceBid){
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        for (String user : users) {
            ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
            apmFlowInstanceRoleUser.setRoleBid(roleBid);
            apmFlowInstanceRoleUser.setInstanceBid(rrBid);
            apmFlowInstanceRoleUser.setUserNo(user);
            apmFlowInstanceRoleUser.setSpaceBid(spaceBid);
            apmFlowInstanceRoleUser.setSpaceAppBid(spaceAppBid);
            apmFlowInstanceRoleUser.setHandleFlag(handleFlag);
            apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
        }
        return apmFlowInstanceRoleUsers;
    }


    /**
     * 将对象转成json字符串
     *
     * @param object 对象
     * @return 集合
     */
    public static List<String> analysisPersions(Object object) {
        List<String> list = new ArrayList<>();
        if (object == null) {
            return list;
        }
        if (object instanceof List) {
            list = JSON.parseArray(object.toString(), String.class);
        } else if (object instanceof String) {
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if (StringUtils.isNotEmpty(objectStr)) {
                list.add(objectStr);
            }
        } else {
            if (object.toString().startsWith(CommonConstant.OPEN_BRACKET)) {
                list = JSON.parseArray(object.toString(), String.class);
            } else {
                list.add(object.toString());
            }
        }
        return list;
    }

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
