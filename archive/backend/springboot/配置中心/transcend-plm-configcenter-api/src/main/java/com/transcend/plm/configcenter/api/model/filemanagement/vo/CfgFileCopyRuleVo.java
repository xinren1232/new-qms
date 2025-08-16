package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CfgFileCopyRuleVo implements java.io.Serializable {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 文件复杂规则名称
     */
    private String name;

    /**
     * 复制类型，move.移动，copy.复制
     */
    private String copyType;

    /**
     * 复制事件，request.请求，change.变化时，其他具体事件
     */
    private String copyEvent;

    /**
     * 超时（秒）
     */
    private Integer timeOut;

    /**
     * 复制模式，1.立即、2.延迟、3.计划、4.手动
     */
    private String copyMode;

    /**
     * 延迟时长（秒）
     */
    private Integer delayDuration;

    /**
     * 过滤方法
     */
    private String filterMethod;

    /**
     * 计划时间
     */
    private PlanTimeParam planTime;

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
     * 文件类型数据
     */
    private List<CfgFileTypeVo> cfgFileTypeVos;

    /**
     * 文件库配置信息
     */
    private List<CfgFileLibraryRuleRelVo> cfgFileLibraryRuleRelVos;

}
