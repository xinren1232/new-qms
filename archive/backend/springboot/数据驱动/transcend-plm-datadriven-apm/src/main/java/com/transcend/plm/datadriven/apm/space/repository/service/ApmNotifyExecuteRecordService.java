package com.transcend.plm.datadriven.apm.space.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmNotifyExecuteRecordService extends IService<ApmNotifyExecuteRecord> {
    /**
     * 方法描述
     *
     * @param id id
     * @return 返回值
     */
    int deleteById(int id);


    /**
     * 查询全部需要执行发送消息的数据
     * 查询今天或今天之前需要执行发送消息或需要立即执行的数据（如果全部查询出来）
     * 后续定时任务执行 可工具空间应用ID分片执行任务
     *
     * @return List<ApmNotifyExecuteRecord>
     */
    List<ApmNotifyExecuteRecord> selectAllExecuteRecord();

    /**
     * 方法描述
     *
     * @param apmNotifyExecuteRecords apmNotifyExecuteRecords
     * @return 返回值
     */
    boolean updateStatusById(List<ApmNotifyExecuteRecord> apmNotifyExecuteRecords);


    /**
     * 查询需要立即发送通知的数据
     * @return {@link List<ApmNotifyExecuteRecord>}
     */
    List<ApmNotifyExecuteRecord> selectImmediateExecuteRecord();

    /**
     * 方法描述
     *
     * @param instanceBids instanceBids
     * @param spaceAppBid  spaceAppBid
     * @return 返回值
     */
    List<ApmNotifyExecuteRecord> listByInstanceBids(List<String> instanceBids, String spaceAppBid);


    /**
     * 根据条件查询需要处理的数据
     *
     * @param nowTime 执行通知的时间
     * @param type    比较类型 =,&gt;=,&lt;=,&lt;,&gt;,&lt;&gt;
     * @return {@link List< ApmNotifyExecuteRecord>}
     * @date 2024/1/22 15:19
     * @author quan.cheng
     */
    List<ApmNotifyExecuteRecord> selectExecuteRecordByParam(String nowTime, String type);

}
