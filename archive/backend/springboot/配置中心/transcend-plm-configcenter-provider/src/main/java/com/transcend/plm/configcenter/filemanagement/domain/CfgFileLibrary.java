package com.transcend.plm.configcenter.filemanagement.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文件库配置
 * @TableName cfg_file_library
 */
@TableName(value ="cfg_file_library")
@Data
public class CfgFileLibrary implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    private String code;

    /**
     * 文件库名称
     */
    private String name;

    /**
     * 归属地，用字典值
     */
    private String address;

    /**
     * 文件库url
     */
    private String url;

    /**
     * url规则
     */
    private String urlRule;

    /**
     * 描述
     */
    private String description;

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

    /**
     * 节点地址
     */
    private String endpoint;

    /**
     *访问秘钥
     */
    private String accessKey;

    /**
     *秘密密钥
     */
    private String secretKey;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}