package com.transcend.plm.datadriven.notify.vo;

import com.transcend.plm.datadriven.notify.dto.NotifyConfigTriggerDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyConfigVo {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属类型,1.操作事件，2.触发事件
     */
    private String type;

    /**
     * 业务类型（OBJECT/APP/INS）
     */
    private String bizType;

    /**
     * 业务bid
     */
    private String bizBid;

    /**
     * 通知角色编码
     */
    private List<String> notifyRoleCodes;

    /**
     * 通知人员工号
     */
    private List<String> notifyJobnumbers;

    /**
     * 通知方式，1.飞书，2.邮件，3.飞书和邮件
     */
    private String notifyWay;

    /**
     * 通知内容，不填程序默认内容，{名称},替换成对象实例名称
     */
    private String notifyContent;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 说明
     */
    private String description;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private int enableFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 发送前校验
     */
    private Boolean sendCheck;

    /**
     * 执行类
     */
    private String executeClass;

    /**
     * type=2时，触发事件相关配置
     */

    private NotifyConfigTriggerVo notifyConfigTriggerVo;

    private NotifyConfigOperateVo notifyConfigOperateVo;

    private NotifyTimeRuleVo notifyTimeRuleVo;
}
