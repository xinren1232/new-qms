package com.transcend.plm.datadriven.notify.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.transcend.plm.datadriven.notify.domain.NotifyConfigOperate;
import com.transcend.plm.datadriven.notify.domain.NotifyTimeRule;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
@Builder(toBuilder = true)
public class NotifyConfigDto {

    /**
     * 租户编码
     */
    private String tenantCode;
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
     * 删除标识
     */
    private Boolean deleteFlag;

    private int enableFlag;

    private Integer enableFlagInt;

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
     * 通知内容，不填程序默认内容，[名称],替换成对象实例名称
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
     * type=1时，操作事件相关配置
     */
    private NotifyConfigOperate notifyConfigOperate;

    /**
     * type=2时，触发事件相关配置
     */
    private NotifyConfigTriggerDto notifyConfigTriggerDto;

    /**
     * 通知时间规则
     */
    private NotifyTimeRule notifyTimeRule;

    /**
     * 发送前校验
     */
    private Boolean sendCheck;

    /**
     * 执行类
     */
    private String executeClass;

    /**
     * 发送前校验，当全部条件满足时
     */
    private NotifyConfigTriggerDto sendConfigTriggerDto;
}
