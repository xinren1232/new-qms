package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CfgFileTypeVo implements Serializable {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 文件类型编码
     */
    private String code;

    /**
     * 文件类型名称
     */
    private String name;

    /**
     * 后缀名
     */
    private String suffixName;

    /**
     * 匹配名称
     */
    private String matching;

    /**
     * 读取规则
     */
    private Integer readRule;

    /**
     * 存储规则
     */
    private String storageRule;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 描述
     */
    private String description;

    /**
     * 文件查看器id集合
     */
    private List<String> fileViewerBids;

    /**
     * 文件查看器集合
     */
    private List<CfgFileViewerVo> cfgFileViewerVos;

    /**
     * 是否是默认库，0不是，1是
     */
    private Boolean defaultFlag;

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
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;
}
