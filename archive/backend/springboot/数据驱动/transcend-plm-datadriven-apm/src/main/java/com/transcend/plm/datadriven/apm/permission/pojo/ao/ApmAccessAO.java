package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmAccessAO {

    private Integer id;

    /**
     *
     */
    private String bid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 资源
     */
    private String resource;

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

    /**
     * 类型：菜单，按钮，接口，对象
     */
    private String type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;
}
