package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Describe
 * @author yuhao.qiu
 * @Date 2023/10/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApmActionConfigVo {

    private String bid;

    private String action;

    private String actionName;

    private String resource;

    private String type;

    /**
     * 可见性配置
     */
    private List<FieldConditionParam> visibleConfig;

    /**
     * 执行操作配置
     */
    private Map<String, String> operationConfig;

    /**
     * 描述
     */
    private String description;

    private List<ApmRoleVO> roles;

    private String createdBy;

    private Date createdTime;
    /**
     * 视图配置
     */
    private Map<String, String> viewConfig;

    /**
     * 方法配置
     */
    private Map<String, String> methodConfig;

    private Integer sort;

    private String icon;

    private Integer deleteFlag;

    private Integer enableFlag;

    /**
     * 按钮展示位置
     */
    private String showLocation;
}
