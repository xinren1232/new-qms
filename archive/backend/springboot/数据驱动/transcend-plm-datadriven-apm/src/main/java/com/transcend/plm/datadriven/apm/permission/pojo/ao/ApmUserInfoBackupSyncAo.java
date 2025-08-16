package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 同步备份用户信息参数
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/27 14:54
 */
@Data
public class ApmUserInfoBackupSyncAo implements Serializable {

    /**
     * 是否进行全量刷新
     */
    private Boolean fullRefresh;

    /**
     * 同步参数
     */
    private List<SyncParams> syncParams;


    @Data
    public static class SyncParams implements Serializable {

        /**
         * 模型编码
         */
        private String modelCode;
        /**
         * 包含有用户信息的字段
         */
        private List<String> fields;
    }
}
