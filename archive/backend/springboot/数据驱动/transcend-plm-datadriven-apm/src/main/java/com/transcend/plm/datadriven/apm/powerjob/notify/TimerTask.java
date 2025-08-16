/*
package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.mapstruct.ApmNotifyExecuteRecordHisConverter;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.notice.PushCenterMailBuilder;
import com.transcend.plm.datadriven.apm.notice.PushCenterProperties;
import com.transcend.plm.datadriven.apm.notice.PushSendResult;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecordHis;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordHisService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

*/
/**
 * 任务包装类(这里也可以将工作任务以线程变量的方式去传入)
 *
 * @author quan.cheng
 * @title TimerTask
 * @date 2024/1/24 17:56
 * @description TODO
 *//*

@Data
@Slf4j
public class TimerTask implements Runnable {


    private ApmNotifyExecuteRecordService apmNotifyExecuteRecordService = PlmContextHolder.getBean(ApmNotifyExecuteRecordService.class);

    private static ApmNotifyExecuteRecordHisService apmNotifyExecuteRecordHisService = PlmContextHolder.getBean(ApmNotifyExecuteRecordHisService.class);
    */
/**
     * 延时时间
     *//*

    private long delayMs;
    */
/**
     * 任务所在的entry
     *//*

    private TimerTaskEntry timerTaskEntry;

    private ApmNotifyExecuteRecord extractedRecord;


    public TimerTask(ApmNotifyExecuteRecord extractedRecord, long delayMs) {
        this.extractedRecord = extractedRecord;
        this.delayMs = delayMs;
        this.timerTaskEntry = null;
    }

    public synchronized void setTimerTaskEntry(TimerTaskEntry entry) {
        // 如果这个timetask已经被一个已存在的TimerTaskEntry持有,先移除一个
        if (timerTaskEntry != null && timerTaskEntry != entry) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = entry;
    }

    public TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }

    @Override
    public void run() {
        log.info("TimerTask run");
        // 根据插入的执行任务
        if (extractedRecord != null) {
            // 执行发送任务
            boolean result = pushMsg(extractedRecord);
        }
        log.info("============={}任务执行", extractedRecord);
    }

    */
/**
     * 执行发送任务
     *
     * @param apmNotifyExecuteRecord 发送任务
     * @return boolean
     *//*

    private boolean pushMsg(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        if (apmNotifyExecuteRecord == null) {
            return false;
        }
        //将需要发送的人员JSON数组转数组
        Object notifyJobNumbers = apmNotifyExecuteRecord.getNotifyJobnumbers();
        List<String> jobNumbers = JSON.parseArray(notifyJobNumbers.toString(), String.class);

        // 发送飞书消息
        if ("1".equals(apmNotifyExecuteRecord.getNotifyWay())) {
            try {
                PushSendResult sendResult = PushCenterFeishuBuilder.builder()
                        .title(apmNotifyExecuteRecord.getNotifyTitle())
                        .content(apmNotifyExecuteRecord.getNotifyContent())
                        .url(apmNotifyExecuteRecord.getUrl())
                        .receivers(jobNumbers)
                        .send();

                log.info("发送飞书消息成功");
                extractedSendResultSuccess(apmNotifyExecuteRecord, sendResult);

            } catch (Exception e) {
                // 发送失败,记录失败日志
                log.error("发送飞书消息失败", e);
                //更新发送失败的记录
                extractedSendResultFail(apmNotifyExecuteRecord, e.getMessage());
            }

            return true;
        }
        // 发送邮件
        if ("2".equals(apmNotifyExecuteRecord.getNotifyWay())) {
            try {
                PushSendResult pushSendResult = PushCenterMailBuilder.builder()
                        .subject(apmNotifyExecuteRecord.getNotifyTitle())
                        .content(apmNotifyExecuteRecord.getNotifyContent())
                        .mailMainReceivers(jobNumbers)
                        .mailCopyReceivers(jobNumbers)
                        .send();
                log.info("发送邮件成功");
                extractedSendResultSuccess(apmNotifyExecuteRecord, pushSendResult);
            } catch (Exception e) {
                // 发送失败,记录失败日志
                log.error("发送邮件失败", e);
                extractedSendResultFail(apmNotifyExecuteRecord, e.getMessage());

            }
            return true;
        }
        // 分别发送飞书和邮件消息
        if ("3".equals(apmNotifyExecuteRecord.getNotifyWay())) {
            try {


                PushSendResult sendResult =  PushCenterFeishuBuilder.builder()
                        .title(apmNotifyExecuteRecord.getNotifyTitle())
                        .content(apmNotifyExecuteRecord.getNotifyContent())
                        .url(apmNotifyExecuteRecord.getUrl())
                        .receivers(jobNumbers)
                        .send();
                extractedSendResultSuccess(apmNotifyExecuteRecord, sendResult);
                log.info("发送飞书消息成功");
            } catch (Exception e) {
                // 发送失败,记录失败日志
                log.error("发送飞书消息失败", e);
                extractedSendResultFail(apmNotifyExecuteRecord, e.getMessage());
            }

            try {
                PushCenterMailBuilder.builder()
                        .subject(apmNotifyExecuteRecord.getNotifyTitle())
                        .content(apmNotifyExecuteRecord.getNotifyContent())
                        .mailMainReceivers(jobNumbers)
                        .mailCopyReceivers(jobNumbers)
                        .send();
                log.info("发送邮件成功");
            } catch (Exception e) {
                // 发送失败,记录失败日志
                log.error("发送邮件失败", e);

            }
        }

        return true;
    }

    */
/**
     * TODO
     *
     * @param apmNotifyExecuteRecord 通知执行记录
     * @param sendResult             消息发送结果
     * @date 2024/1/29 13:53
     * @author quan.cheng
     *//*

    private  void extractedSendResultSuccess(ApmNotifyExecuteRecord apmNotifyExecuteRecord, PushSendResult sendResult) {
        if (sendResult.isSuccess()) {
            //删除已发送的记录
            removeById(apmNotifyExecuteRecord.getId());
            //将数据保存到历史表
            saveHistory(apmNotifyExecuteRecord);
        } else {
            // 发送失败,记录失败日志
            log.error("发送飞书消息失败", sendResult.getMessage());
            //更新发送失败的记录
            extractedSendResultFail(apmNotifyExecuteRecord, sendResult.getMessage());
        }
    }

    */
/**
     * 保存发送成功的数据到历史表中
     *
     * @param apmNotifyExecuteRecord 通知执行记录
     * @date 2024/1/29 14:42
     * @author quan.cheng
     *//*

    private static void saveHistory(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        ApmNotifyExecuteRecordHis apmNotifyExecuteRecordHis = ApmNotifyExecuteRecordHisConverter.INSTANCE.po2His(apmNotifyExecuteRecord);
        apmNotifyExecuteRecordHisService.save(apmNotifyExecuteRecordHis);
    }

    */
/**
     * 根据id删除记录
     *
     * @param id 通知执行记录id
     *//*

    private  void removeById(Integer id) {
        apmNotifyExecuteRecordService.deleteById(id);
    }

    private void extractedSendResultFail(ApmNotifyExecuteRecord apmNotifyExecuteRecord, String msg) {
        apmNotifyExecuteRecord.setNofifyResult(2);
        // 发送次数 在原有基础上加1
        apmNotifyExecuteRecord.setNofifyRetryCount(apmNotifyExecuteRecord.getNofifyRetryCount() + 1);
        apmNotifyExecuteRecord.setNofifyResultMsg(msg);
        updateApmNotifyExecuteRecord(apmNotifyExecuteRecord);
    }

    */
/**
     * 发送失败更新通知执行记录
     *
     * @param apmNotifyExecuteRecord 通知执行记录
     * @date 2024/1/29 14:41
     *//*

    private  void updateApmNotifyExecuteRecord(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        apmNotifyExecuteRecordService.updateById(apmNotifyExecuteRecord);
    }
}*/
