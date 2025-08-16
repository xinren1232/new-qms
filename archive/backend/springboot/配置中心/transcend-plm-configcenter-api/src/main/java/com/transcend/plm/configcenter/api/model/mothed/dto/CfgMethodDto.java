package com.transcend.plm.configcenter.api.model.mothed.dto;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:20
 **/
@ApiModel("标准方法")
@Data
public class CfgMethodDto extends BaseDto {

    private String code;

    /**
     * 方法名称
     */
    private String name;

    /**
     * 方法描述说明
     */
    private String description;

    /**
     * 区分，主要用于模板何时加载
     */
    private String tag;

    /**
     * 分组
     */
    private String methodGroup;

    /**
     * 语言类型
     */
    private String languageType;

    /**
     * 模板标识（1-模板，0-非模板）
     */
    private Boolean templFlag;

    /**
     * 方法文件描述
     */
    private String referenceClassName;

    /**
     * 方法签名
     */
    private String executeMethodName;

    /**
     * 是否异步（1-异步，0-非异步）
     */
    private Boolean sync;

    /**
     * 方法代码
     */
    private String content;


    private static final long serialVersionUID = 1L;
}
