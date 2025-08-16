package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppDto {

    /**
     * 类型
     */
    private String type;
    /**
     * 类型code
     */
    private String typeCode;
    /**
     * 对象应用编码
     */
    private String modelCode;

    /**
     * 对象应用名称
     */
    private String name;


    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标url
     */
    private String iconUrl;

    /**
     * 扩展字段
     */
    private JSONObject configContent;

    /**
     * 复制实例标识（空:不复制，APP_INSTANCE:默认复制应用实例)
     */
    private String copyInstanceModel;

    /**
     * 打开模式：空：默认打开列表，APP_INSTANCE:默认打开应用实例
     */
    private String openModel;


}
