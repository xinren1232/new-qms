package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Describe 查询用户的操作配置信息
 * @author yuhao.qiu
 * @Date 2023/10/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmActionConfigUserVo {

    private String bid;

    private String action;

    private String actionName;

    private String resource;

    private List<FieldConditionParam> visibleConfig;

    private Map<String, String> operationConfig;

    private String description;

    private String type;

    /** 展示在更多里 */
    private Boolean more;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    private String actionType;

    /**
     * 方法数据
     */
    private Map<String, String> methodConfig;

    /**
     * 按钮展示位置
     */
    private String showLocation;

}
