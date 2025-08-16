package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmAppViewConfigVo {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间BID
     */
    private String spaceBid;

    /**
     * 应用BID
     */
    private String spaceAppBid;


    /**
     * Tab的BID
     */
    private String tabBid;

    /**
     * 视图名称
     */
    private String viewName;

    /**
     * 视图描述
     */
    private String viewDesc;

    /**
     * 视图类型，1.条件视图，2.导航视图
     */
    private Integer viewType;

    /**
     * 导航应用BID
     */
    private String navSpaceAppBid;

    /**
     * 导航显示编码，tableView：表格，multiTreeView：多维表格，treeView：层级
     */
    private String navCode;

    /**
     * 导航视图配置内容（各个模式自定义）
     */
    private Map<String, Object> navConfigContent;

    /**
     * 导航视图和源对象属性映射关系,KEY 为导航对象bid,value为目标对象属性
     */
    private Map<String,String> navAttrConfig;

    /**
     * 显示类型，labe.标签显示，menu.菜单显示
     */
    private String showType;

    /**
     * 视图配置条件
     */
    private Map<String,Object> viewCondition;

    /**
     * 协作者
     */
    private List<String> teamWorkers;

    /**
     * 空间角色bid
     */
    private List<String> spaceRoleBids;

    /**
     * 部门id
     */
    private List<String> departmentIds;

    /**
     * 人员id
     */
    private List<String> userIds;

    /**
     * 排序
     */
    private int sort;

    /**
     * 是否默认视图
     */

    private boolean isDefault;

    /**
     * 是否可以编辑
     */
    private boolean isEdit;

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
