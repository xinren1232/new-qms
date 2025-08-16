package com.transcend.plm.datadriven.common.pojo.po;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.transsion.framework.common.StringUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author unknown
 */
public class TranscendTableNameHandler implements TableNameHandler {

    /**
     * 记录哪些表需要加租户编码
     */
    private List<String> tableNames;

    private static final ThreadLocal<String> tenantCode = new ThreadLocal<>();

    public static void setTenantCode(String code) {
        tenantCode.set(code);
    }

    public static void remove() {
        tenantCode.remove();
    }

    public TranscendTableNameHandler(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (!CollectionUtils.isEmpty(tableNames) && tableNames.contains(tableName) && StringUtil.isNotBlank(tenantCode.get())) {
            return tenantCode.get() + "_" + tableName;
        }
        return tableName;
    }
}
