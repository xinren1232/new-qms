package com.transcend.plm.datadriven.apm.flow.pojo.qo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色AO
 * @createTime 2023-09-20 15:01:00
 */
@Data
public class ApmRoleQO {
    private Integer id;

    /**
     *
     */
    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 域id
     */
    private String sphereBid;

    /**
     * 业务id
     */
    @NotBlank(message = "业务id不能为空")
    private String bizBid;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 空间下对象实例bid
     */
    private String instanceBid;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;
}
