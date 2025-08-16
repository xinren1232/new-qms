package com.transcend.plm.datadriven.api.model.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
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
}
