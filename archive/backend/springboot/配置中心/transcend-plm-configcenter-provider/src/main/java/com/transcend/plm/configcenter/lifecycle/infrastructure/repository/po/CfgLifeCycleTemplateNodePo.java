package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName cfg_life_cycle_template_node
 */
@TableName(value ="cfg_life_cycle_template_node")
@Data
public class CfgLifeCycleTemplateNodePo extends BasePoEntity implements Serializable {




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
     * 节点位置前端使用
     */
    private String layout;


    /**
     * 生命周期状态名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否关键路径，0否，1是
     */
    private Integer keyPathFlag;


    /**
     * 所属组编码
     */
    private String groupCode;
    /**
     * 状态（0未启用，1启用，2禁用，默认启用）
     */
    private Integer enableFlag = 1;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}