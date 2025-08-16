package com.transcend.plm.configcenter.view.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 视图表
 * @TableName cfg_view
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Data
@TableName(value ="cfg_view",autoResultMap = true)
public class CfgViewPo extends BasePoEntity implements Serializable {

    /**
     * 所属bid
     */
    private String belongBid;

    /**
     * 视图作用域
     */
    private String viewScope;

    /**
     * 编码
     */
    private String type;

    /**
     * 客户端类型（web,app）
     */
    private String clientType;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 对象code
     */
    private String modelCode;

    /**
     * tag
     */
    private String tag;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 视图类型
     */
    private String viewType;

    /**
     * 视图内容
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> content;

    /**
     * 视图内容元数据
     */
    @TableField(value = "meta_model", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<CfgViewMetaDto> metaList;

    private static final long serialVersionUID = 1L;

}