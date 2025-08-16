package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmSpaceAppAo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间业务id
     */
    private String spaceBid;

    /**
     * 领域bid
     */
    private String sphereBid;
    /**
     * 类型code
     */
    private String typeCode;
    /**
     * 类型
     */
    private String type;

    /**
     * 模型code
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
     * 复制实例标识（空:不复制，APP_INSTANCE:默认复制应用实例)
     */
    private String copyInstanceModel;

    /**
     * 打开模式：空：默认打开列表，APP_INSTANCE:默认打开应用实例
     */
    private String openModel;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标志
     */
    private Integer deleteFlag;

    /**
     * 启用标志
     */
    private Integer enableFlag;

    /**
     * 是否显示
     */
    private Integer visibleFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 图标url
     */
    private String iconUrl;

    /**
     * 扩展字段
     */
    private JSONObject configContent;

    /**
     * 是否版本对象
     */
    private Boolean isVersionObject;

    /**
     * 分组名称
     */
    private String groupName;
}