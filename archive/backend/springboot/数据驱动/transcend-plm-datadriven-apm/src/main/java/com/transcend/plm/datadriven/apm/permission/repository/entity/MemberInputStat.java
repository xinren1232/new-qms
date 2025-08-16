package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_role_identity
 */
@TableName(value ="apm_member_input_stat")
@Data
@Accessors(chain = true)
public class MemberInputStat implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 空间bid
     */
    @TableField(value = "space_bid")
    private String spaceBid;

    /**
     * 应用bid
     */
    @TableField(value = "space_app_bid")
    private String spaceAppBid;

    /**
     * 业务bid(项目实例bid
     */
    @TableField(value = "biz_bid")
    private String bizBid;

    /**
     * 工号
     */
    @TableField(value = "job_number")
    private String jobNumber;

    /**
     * 姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 月份
     */
    @TableField(value = "month")
    private String month;

    /**
     * 人员投入百分比
     */
    @TableField(value = "input_percentage")
    private Integer inputPercentage;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;
    /**
     * 删除标志
     */
    @TableField(value = "delete_flag")
    @TableLogic
    private Integer deleteFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}