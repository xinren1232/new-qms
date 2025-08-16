package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowTemplateNodeAO {

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
     * 前端当前节点唯一标识
     */
    private String webBid;

    /**
     * 前置节点前端唯一标识集合
     */
    private List<String> beforeWebBids;

    /**
     * 前置节点bid集合
     */
    private List<String> beforeNodeBids;

    private List<String> nextWebBids;

    private Integer sort;

    /**
     * 是否显示帮助，0否，1是
     */
    private Boolean showHelpTipFlag;

    /**
     * 节点帮助提示内容
     */
    private String nodeHelpTip;

    /**
     * 通知标识
     */
    private Boolean notifyFlag;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 线名称
     */
    private String lineName;

    /**
     * 节点对应的角色bids
     */
    private List<String> nodeRoleBids;

    /**
     * 模板节点是否允许删除，0.不允许，1.允许
     */
    private Boolean nodeDeleteFlag;

    /**
     * 是否关键路径，0否，1是
     */
    private Boolean keyPathFlag;

    /**
     * 是否允许回退，true允许，false不允许
     */
    private Boolean allowBack;

    /**
     * 是否允许回退 未完成节点，true允许，false不允许
     */
    private Boolean allowBackNotCompleted;
    /**
     * 允许删除的角色bids
     */
    private List<String> nodeDeleteRoleBids;

    /**
     * 节点类型，0.开始节点，1.进行中节点，2.结束节点
     */
    private Integer nodeType;

    /**
     * 完成方式，0.自动完成，1.单人确认完成，2.多人确认完成
     */
    private Integer complateType;

    /**
     * 完成操作授权角色bids
     */
    private List<String> complateRoleBids;

    /**
     * 生命周期编码
     */
    private String lifeCycleCode;

    private String lifeCycleCodeType;

    /**
     * 显示条件，1.默认可见，0.在某条件下可见
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

    /**
     * displayCondition = 2,ampFlowNodeDisplayConditionAOList配置可惜条件
     */
    private List<ApmFlowNodeDisplayConditionAO> apmFlowNodeDisplayConditionAOList;

    /**
     * 节点下配置的任务
     */
    private List<ApmFlowNodeTaskAO> apmFlowNodeTaskAOList;

    /**
     * 配置的事件
     */
    private List<ApmFlowNodeEventAO> apmFlowNodeEventAOList;

    /**
     * 节点位置前端使用
     */
    private String layout;

    /**
     * 节点流转配置
     */
    private List<ApmFlowNodeDirectionAO> apmFlowNodeDirectionAOs;

}
