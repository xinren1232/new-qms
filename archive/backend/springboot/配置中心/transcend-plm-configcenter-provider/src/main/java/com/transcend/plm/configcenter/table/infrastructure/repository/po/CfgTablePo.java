package com.transcend.plm.configcenter.table.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 表属定义表
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 15:59
 */
@Data
@TableName("cfg_table")
public class CfgTablePo extends BasePoEntity implements Serializable {

    /**
     * 逻辑表名称
     */
    private String logicTableName;

    /**
     * 策略编码
     */
    private String strategyCode;

    /**
     * 分片字段
     */
    private String shardColumn;

    /**
     * 分片数量
     */
    private String shardNum;

    /**
     * 策略规则
     */
    private String rule;

    /**
     * 租户
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 直接使用表名
     */
    private Byte useLogicTableName = 0;

    private static final long serialVersionUID = 1L;

}
