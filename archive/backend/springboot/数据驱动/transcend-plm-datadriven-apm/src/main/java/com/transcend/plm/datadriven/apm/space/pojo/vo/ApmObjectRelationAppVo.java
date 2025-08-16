package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationAttrVo;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.pojo.dto.RelationActionPermission;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 空间应用关系实体
 * @date 2023/10/14 11:39
 **/
@Data
public class ApmObjectRelationAppVo implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 关系编码
     */
    private String bid;


    /**
     * TAB编码
     */
    private String tabBid;

    /**
     * 关系名称
     */
    private String name;

    /**
     * tab名称
     */
    private String tabName;

    /**
     * 描述
     */
    private String description;

    /**
     * 源对象编码
     */
    private String sourceModelCode;

    /**
     * 关系表自身编码
     */
    private String modelCode;

    /**
     * 源对象名称
     */
    private String sourceObjName;

    /**
     * 目标对象编码
     */
    private String targetModelCode;
    /**
     * 目标对象名称
     */
    private String targetObjName;

    /**
     * 关联行为（固定：关联对象版本固定创建时的版本，浮动：关联对象版本一直用最新的）
     */
    private String behavior;

    /**
     * 关联类型（仅创建：关联对象需要自己创建，仅选取：关联对象需要选择，两者皆可）
     */
    private String type;

    /**
     * 关联项必填（应用时 关联的对象实例是否必需）
     */
    private Byte isRequired;

    /**
     * 允许的最大实例数量
     */
    private Integer maxNumber;

    /**
     * 允许的最小实例数量
     */
    private Integer minNumber;

    /**
     * 应用时是否隐藏tab
     */
    private Byte hideTab;

    /**
     * 标签排序
     */
    private Integer sort;

    /**
     * 检出以浮动方式检出检入
     */
    private Byte floatBehavior;

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
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    /**
     * 是否内置
     */
    boolean inner;

    /**
     * 目标对象展示方式(table:表格，tree:树形)
     */
    private String showType;

    private String source;

    private String spaceBid;

    private String spaceAppBid;

    /**
     * 是否每次切换都从新加载数据
     */
    private Boolean isRefresh;

    private static final long serialVersionUID = 1L;

    private List<CfgObjectRelationAttrVo> relationAttr;

    private ModelMixQo configContent;

    private List<ModelFilterQo> showConditionContent;

    private boolean checked;

    private String code;

    private List<Map> multiTreeContent;

    private String viewModelCode;

    private MultiAppConfig multiAppTreeContent;

    /**
     * 显示 视图模式
     */
    private List<String> showViewModels;

    private List<RelationActionPermission> relationActionPermissionList;

    private String spaceAppTabBid;

    private Boolean inverseQuery;
}
