package com.transcend.plm.datadriven.notify.service;

import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import com.transcend.plm.datadriven.notify.vo.NotifyExecuteTimeVo;
import com.transcend.plm.datadriven.notify.vo.NotifyTimeRuleVo;

import java.util.List;
import java.util.Map;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public interface NotifyAppService {
    /**
     * 通知配置保存
     *
     * @param dto 通知配置
     * @return boolean
     */
    boolean saveNotifyConfig(NotifyConfigDto dto);

    /**
     * 通知配置批量保存
     *
     * @param dtos 通知配置
     * @return boolean true
     */
    boolean saveNotifyConfigs(List<NotifyConfigDto> dtos);

    /**
     * 通知配置查询
     *
     * @param dto 通知配置
     * @return {@link List<NotifyConfigVo> }
     */
    List<NotifyConfigVo> listNotifyConfig(NotifyConfigDto dto);

    /**
     * 根据业务类型、业务编号获取通知配置
     *
     * @param bizType     业务类型
     * @param bizBid      业务编号
     * @param operateType 操作类型
     * @param tenantCode  租户编号
     * @return {@link NotifyConfigVo }
     */
    NotifyConfigVo getOperateConfig(String bizType, String bizBid, String operateType, String tenantCode);

    /**
     * 解析通知时间规则
     *
     * @param notifyTimeRuleVo 时间规则
     * @param insData          实例数据
     * @return {@link NotifyExecuteTimeVo }
     */
    NotifyExecuteTimeVo getNotifyExecuteTime(NotifyTimeRuleVo notifyTimeRuleVo, Map<String, Object> insData);
}
