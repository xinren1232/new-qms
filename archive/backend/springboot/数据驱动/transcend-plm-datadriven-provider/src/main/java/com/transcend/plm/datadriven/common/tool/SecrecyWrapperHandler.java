package com.transcend.plm.datadriven.common.tool;

import com.transcend.framework.common.util.StringUtil;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 保密需求条件处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/18 10:37
 */
public class SecrecyWrapperHandler {
    private static final String[] MODEL_CODE_ARRAY = {"A01", "A02"};


    /**
     * 自动添加保密条件
     *
     * @param wrappers 查询条件
     * @param userNo   工号
     * @return 查询条件
     */
    public static List<QueryWrapper> autoAddSecrecyWrapper(List<QueryWrapper> wrappers, String modelCode, String userNo) {
        if (!ArrayUtils.contains(MODEL_CODE_ARRAY, modelCode)) {
            return wrappers;
        }
        if (wrappers == null) {
            wrappers = new ArrayList<>(16);
        }
        if (!wrappers.isEmpty()) {
            wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        }
        wrappers.add(getSecrecyWrapper(userNo));
        return wrappers;
    }


    /**
     * 获取保密条件
     *
     * @param userNo 工号
     * @return 查询条件
     */
    @NotNull
    public static QueryWrapper getSecrecyWrapper(String userNo) {
        return getSecrecyWrapperAndPermission(userNo, "");
    }



    /**
     * 获取保密条件
     *
     * @param userNo 工号
     * @return 查询条件
     */
    @NotNull
    public static QueryWrapper getSecrecyWrapperAndPermission(String userNo, String permissionSql) {
        //复杂表达式底层会手动拼接，需要使用sql拼接
        String formatSql = getSecrecyWrapperAndPermissionForStringSql(userNo, permissionSql);
        return new QueryWrapper(true).setSqlRelation(formatSql);
    }

    @NotNull
    public static String getSecrecyWrapperAndPermissionForStringSql(String userNo, String permissionSql) {
        if(StringUtil.isNotBlank(permissionSql)) {
            permissionSql = " AND " + permissionSql;
        }
        String sql = " (COALESCE(is_conl_require, '') <> 'IS_YES'" +
                permissionSql +
                " OR (is_conl_require = 'IS_YES'";
//                " AND (created_by = '%s'" +
//                " OR owner = '%s'" +
//                " OR JSON_CONTAINS(confidential_member, JSON_ARRAY('%s'))" +
//                " OR JSON_CONTAINS(person_responsible, JSON_ARRAY('%s'))" +
//                " OR JSON_CONTAINS(handler, JSON_ARRAY('%s'))" +
//                " )))";

        String sqlFormat = " AND (created_by = '%s'" +
                " OR owner = '%s'" +
                " OR JSON_CONTAINS(confidential_member, JSON_ARRAY('%s'))" +
                " OR JSON_CONTAINS(person_responsible, JSON_ARRAY('%s'))" +
                " OR JSON_CONTAINS(handler, JSON_ARRAY('%s'))" +
                " )))";
        String formatString = String.format(sqlFormat, userNo, userNo, userNo, userNo, userNo);
        return sql + formatString;
    }

    /**
     * 获取保密条件
     * @param userNo 工号
     * @param modelCode 模型编码
     * @param permissionSql 权限sql表达式
     * @return 查询条件
     */
    @NotNull
    public static String getSecrecyWrapperAndPermissionFilterModelForStringSql(String userNo, String modelCode, String permissionSql) {
        // 暂时只开放 IR SR, 后续陆续开放
        if (!ArrayUtils.contains(MODEL_CODE_ARRAY, modelCode)) {
            return permissionSql;
        }
        return getSecrecyWrapperAndPermissionForStringSql(userNo, permissionSql);
    }


}
