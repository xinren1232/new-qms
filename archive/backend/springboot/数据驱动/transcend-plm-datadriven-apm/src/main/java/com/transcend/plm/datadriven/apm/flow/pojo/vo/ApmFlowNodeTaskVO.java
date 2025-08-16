package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeTaskVO {
    /**
     *
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 节点业务bid
     */
    private String nodeBid;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务状态，0未完成，1完成
     */
    private Integer taskState;

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
}
