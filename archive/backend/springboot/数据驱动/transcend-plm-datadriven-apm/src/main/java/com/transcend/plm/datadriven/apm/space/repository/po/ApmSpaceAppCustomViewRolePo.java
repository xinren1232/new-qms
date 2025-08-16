package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author unknown
 * @TableName apm_space_app_custom_view_role
 */
@TableName(value = "apm_space_app_custom_view_role")
@Data
@Accessors(chain = true)
public class ApmSpaceAppCustomViewRolePo implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色bid
     */
    @TableField(value = "role_bid")
    private String roleBid;

    /**
     * 自定义视图bid
     */
    @TableField(value = "custom_view_bid")
    private String customViewBid;

    /**
     * 空间应用
     */
    @TableField(value = "space_app_bid")
    private String spaceAppBid;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static ApmSpaceAppCustomViewRolePo of() {
        return new ApmSpaceAppCustomViewRolePo();
    }
}