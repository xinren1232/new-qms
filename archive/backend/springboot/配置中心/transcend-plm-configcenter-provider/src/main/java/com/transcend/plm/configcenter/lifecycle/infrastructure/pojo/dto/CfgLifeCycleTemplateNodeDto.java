package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto;

import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateObjRelPo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class CfgLifeCycleTemplateNodeDto implements Serializable {

    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 生命周期状态编码
     */
    private String lifeCycleCode;

    /**
     * 用途-用于标记改状态是什么（比如开始或者结束状态的标识）
     */
    private String use;

    /**
     * 节点说明
     */
    private String description;

    /**
     * 节点标签
     */
    private String flag;

    /**
     * 节点图标
     */
    private String avatar;

    /**
     * 关系行为作用域，1：针对全目标对象，2：按照选择目标对象优先级
     */
    private Integer behaviorScope;

    /**
     * 关联行为（fixed:固定,float:浮动）
     */
    private String behavior;

    /**
     * 绑定流程
     */
    private String bindProcess;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否关键路径，0否，1是
     */
    private Integer keyPathFlag;

    /**
     * 节点位置前端使用
     */
    private String layout;

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
    private Integer deleteFlag;

    /**
     * 状态（启用标志，0未启用，1启用，2禁用）
     */
    private Integer enableFlag = 1;
    /**
     * 生命周期状态名称
     */
    private String name;
    /**
     * 所属组编码
     */
    private String groupCode;

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前节点关系")
    private List<CfgLifeCycleTemplateObjRelPo> lifeCycTemObjRelList;

}