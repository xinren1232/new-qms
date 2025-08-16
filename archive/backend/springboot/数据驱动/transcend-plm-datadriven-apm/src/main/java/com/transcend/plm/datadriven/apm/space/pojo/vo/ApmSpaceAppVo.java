package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppVo {

    private Long id;

    /**
     * 业务id
     */
    private String bid;
    /**
     * 类型
     */
    private String type;
    /**
     * 类型code
     */
    private String typeCode;

    /**
     * 模型code
     */
    private String modelCode;

    /**
     * 对象应用名称
     */
    private String name;

    /**
     * 空间业务id
     */
    private String spaceBid;

    /**
     * 领域bid
     */
    private String sphereBid;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标url
     */
    private String iconUrl;

    /**
     * 复制实例标识（空:不复制，APP_INSTANCE:默认复制应用实例)
     */
    private String copyInstanceModel;

    /**
     * 打开模式：空：默认打开列表，APP_INSTANCE:默认打开应用实例
     */
    private String openModel;

    /**
     * 启用标识
     */
    private Integer enableFlag;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    /**
     * 是否可见
     */
    private Integer visibleFlag;

    /**
     * 扩展字段
     */
    private JSONObject configContent;

    /**
     * 是否是版本对象
     */
    private Boolean isVersionObject;

    /**
     * 是否支持批量导入
     */
    private boolean batchImport;

    /**
     * 分组名称
     */
    private String groupName;
}
