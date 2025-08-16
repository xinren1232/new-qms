package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@ToString
@Data
public class ApmFlowInstanceNodeVO {
    /**
     *
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 页面业务bid
     */
    private String webBid;

    /**
     * 流程模板节点业务id
     */
    private String templateNodeBid;

    /**
     * 流程模板节点业务id
     */
    private String templateNodeDataBid;

    /**
     * 对象模型编码
     */
    private String modelCode;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;

    /**
     * 实例bid
     */
    private String instanceBid;

    /**
     * 通知标识
     */
    private Boolean notifyFlag;

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
     * 所属流程版本号
     */
    private String version;

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

    private static final long serialVersionUID = 1L;

    /**
     * 是否显示帮助，0否，1是
     */
    private Boolean showHelpTipFlag;

    /**
     * 流程待办状态
     */
    private Integer taskState;

    /**
     * 节点帮助提示内容
     */
    private String nodeHelpTip;


    /**
     * 是否允许回退，true允许，false不允许
     */
    private Boolean allowBack;

    /**
     * 是否允许回退 未完成节点，true允许，false不允许
     */
    private Boolean allowBackNotCompleted;
}
