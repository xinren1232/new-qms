package com.transcend.plm.datadriven.common.constant;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * 角色配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public class RoleConst extends CommonConst{

    /**
     * 角色类型-0系统角色，1业务角色
     */
    public static final Byte SYS_TYPE = 0;
    public static final Byte BIZ_TYPE = 1;

    public static final String SYS_NAME = "系统角色";
    public static final String BIZ_NAME = "业务角色";

    public static final List<String> SYS_GLOBAL_ROLE_TYPE = Lists.newArrayList("SYS_GLOBAL");
    public static final List<String> SYS_INNER_ROLE_TYPE = Lists.newArrayList("SYS_INNER");
    public static final List<String> SYS_BIZ_ROLE_TYPE = Lists.newArrayList("SYS_BIZ");
    public static final List<String> SPACE_ROLE_TYPE = Lists.newArrayList("SPACE");
    public static final Set<String> SYS_ROLE_TYPE_SET = Sets.newHashSet("SYS_GLOBAL","SYS_INNER","SYS_BIZ");
}
