package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowTemplateNodeVO {
    /**
     *
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 业务databid
     */
    private String dataBid;

    /**
     * 是否进行完成检查
     */
    private Boolean completeCheckFlag;

    /**
     * 完成检查事件
     */
    private String completeCheckEvent;

    /**
     * 页面业务bid
     */
    private String webBid;

    /**
     * 前置节点bid集合
     */



    private List<String> beforeNodeBids;

    /**
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 节点状态，0.未开始，1.进行中，2.完成
     */
    private Integer nodeState;

    /**
     * 通知标识
     */
    private Boolean notifyFlag;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 排序
     */
    private Integer sort;
    /**
     * 节点对应的角色bids
     */
    private List<String> nodeRoleBids;

    /**
     * 模板节点是否允许删除，0.不允许，1.允许
     */
    private Boolean nodeDeleteFlag;

    /**
     * 允许删除的角色bids
     */
    private List<String> nodeDeleteRoleBids;

    /**
     * 节点类型，1.开始节点，2.进行中节点，3.结束节点
     */
    private Integer nodeType;

    /**
     * 完成方式，1.自动完成，2.单人确认完成，3.多人确认完成
     */
    private Integer complateType;

    /**
     * 完成操作授权角色bids
     */
    private List<String> complateRoleBids;

    /**
     * 显示条件，1.默认可见，2.在某条件下可见
     */
    private Integer displayCondition;

    /**
     * 条件匹配形式，all.全部匹配，any.满足一个即可
     */
    private String displayConditionMatch;

    /**
     * 当前节点激活，前面路径匹配方式，all.所有完成，any.任一完成
     */
    private String activeMatch;

    private List<ApmFlowNodeDisplayConditionVO> apmFlowNodeDisplayConditionVOList;

    /**
     * 节点下配置的任务
     */
    private List<ApmFlowNodeTaskVO> apmFlowNodeTaskVOList;

    /**
     * 配置的事件
     */
    private List<ApmFlowNodeEventVO> apmFlowNodeEventVOList;

    /**
     * 是否允许回退，true允许，false不允许
     */
    private Boolean allowBack;

    /**
     * 是否允许回退 未完成节点，true允许，false不允许
     */
    private Boolean allowBackNotCompleted;

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
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;

}
