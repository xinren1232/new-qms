package com.transcend.plm.datadriven.apm.constants;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author jie.luo1
 * @version 1.0.0
 * @Description 空间常量类
 * @createTime 2023-10-25 16:04:00
 */
public class SpaceConstant {
    private SpaceConstant() {
    }
    public static final Set<String> DEFAULT_ROLE_SET = Sets.newHashSet(RoleConstant.SPACE_ADMIN_EN, RoleConstant.SPACE_MEMBER_EN, RoleConstant.SPACE_ALL_EN);
    public static final String APP_COPY_MODEL_APP_INSTANCE = "APP_INSTANCE";
    public static final String APP_COPY_MODEL_SPACE_INSTANCE = "SPACE_INSTANCE";
    public static final String APP_COPY_MODEL_TREE_INSTANCE = "TREE_INSTANCE";

    /**
     * 基础空间bid（相当于空间原始池子）
     */
    public static final String ALM_BASE_SPACE_BID = "1342178570115805184";
    /**
     * 删除表示 0 否
     */
    public static final String DELETE_FLAG_NO = "0";

}
