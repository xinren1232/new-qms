package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2023/10/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmAccessOperationAo {

    private String bid;

    private String action;

    private String actionName;

    private String resource;

    private String description;

    private List<FieldConditionParam> visibleConfig;

    private Map<String, String> operationConfig;

    private Map<String, String> viewConfig;

    private Map<String, String> methodConfig;

    private String type;

    private List<String> roleBids;

    private Integer sort;

    private String icon;

    private Integer deleteFlag;

    private Integer enableFlag;
    @ApiModelProperty("按钮展示位置")
    private String showLocation;
}
