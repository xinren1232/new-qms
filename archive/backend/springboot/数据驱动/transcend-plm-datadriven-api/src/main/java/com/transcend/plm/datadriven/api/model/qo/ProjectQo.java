package com.transcend.plm.datadriven.api.model.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: transcend-plm-datadriven
 * @BelongsPackage: com.transcend.plm.datadriven.api.model.qo
 * @Author: WWX
 * @CreateTime: 2024-08-27  10:42
 * @Description: TODO
 * @Version: 1.0
 */
@Builder
@Getter
@Setter
@ToString
public class ProjectQo {

    /**
     * 项目bid
     */
    private String bid;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目名列表（精确匹配）
     */
    private List<String> nameList;
    /**
     * 项目名称列表（精确匹配）
     */
    private List<String> projectNameList;
    /**
     * 项目状态
     */
    private List<String> stateCode;
    /**
     * 项目分类
     */
    private String typeCode;
    /**
     * 项目定级
     */
    private String levelCode;
    /**
     * 产品线
     */
    private String series;
    /**
     * 计划开始时间
     */
    private Date planStartTime;
    /**
     * 计划完成时间
     */
    private Date planEndTime;
    /**
     * 实际开始时间
     */
    private Date actualStartTime;
    /**
     * 实际结束时间
     */
    private Date actualEndTime;
    /**
     * 是否为项目模板
     */
    private Boolean isTempl;
    /**
     * 项目版本
     */
    private Integer version;
    /**
     * 对象编号
     */
    private String objBid;
    /**
     * 是否删除
     */
    private Byte delete;
    /**
     * 项目开关
     */
    private String projectSwitch;
    /**
     * 所属领域
     */
    private String belongingDomain;
    /**
     * ext中projectName
     */
    private String projectName;
    /**
     * 查询的数目限制
     */
    private Integer size;
    /**
     * 对象列表
     */
    private List<String> objectBidList;
    /**
     * 数据bid列表
     */
    private List<String> dataBids;
    /**
     * 权限bid
     */
    private String permission_bid;

    private String type_code;

    private String obj_bid;


    /**
     * 项目编码
     */
    private String code;

    /**
     * 创建时间开始范围
     */
    private Date beginCreatedTime;
    /**
     * 创建时间结束范围
     */
    private Date endCreatedTime;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Long companyId;

    private String platformCode;
    /**
     * 项目分组
     */
    private String project_group;
    /**
     * 项目类型编码
     */
    private List<String> typeCodes;
    /**
     * 是否查项目经理所在部门信息，默认查询
     */
    private List<String> brands;
    /**
     * 项目名称精确查找
     */
    private String nameEqual;

    private static final long serialVersionUID = 1L;
    /**
     * 是否查项目经理所在部门信息，默认查询
     */
    private Boolean queryDeptBelong;

    /**
     * 不包含modelBid
     */
    private String notContainModelBid;
    /**
     * 创建时间范围
     */
    private List<String> createdTimeRange;

    /**
     * 项目code
     */
    private List<String> modelCodes;

    /**
     * modelBid
     */
    private String modelBid;
}
