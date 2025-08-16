package com.transcend.plm.configcenter.common.constant;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色配置常量
 *
 * @author jie.luo1
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
}
