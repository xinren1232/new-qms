package com.transcend.plm.configcenter.attribute.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("标准属性")
public class CfgAttributeVo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 对象模型code
     */
    private String modelCode;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 使用状态，禁用2，启用1，未启用0(默认0)
     */
    private Integer enableFlag;

    /**
     * 分组
     */
    private String groupName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据库字段名称
     */
    private String dbKey;

    /**
     * 是否为空
     */
    private Boolean nullAble;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 国际化语言字典
     */
    private Map<String, String> langDict;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 默认值
     */
    private String defaultValue;

    private static final long serialVersionUID = 1L;
}
