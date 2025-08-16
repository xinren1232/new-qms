package com.transcend.plm.datadriven.apm.powerjob.notify.analysis.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.mapstruct.ApmNotifyExecuteRecordHisConverter;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.notice.PushCenterMailBuilder;
import com.transcend.plm.datadriven.apm.notice.PushSendResult;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.INotifyMessageService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecordHis;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordHisService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@Slf4j
@Service
public class NotifyMessageServiceImpl implements INotifyMessageService {
    @Resource
    private ApmNotifyExecuteRecordHisService apmNotifyExecuteRecordHisService;
    @Resource
    private ApmNotifyExecuteRecordService apmNotifyExecuteRecordService;

    @Override
    @Async
    public void pushMsg(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        if (apmNotifyExecuteRecord == null) {
            return;
        }
        //将需要发送的人员JSON数组转数组
        Object notifyJobNumbers = apmNotifyExecuteRecord.getNotifyJobnumbers();
        List<String> jobNumbers = JSON.parseArray(notifyJobNumbers.toString(), String.class);

        //1.飞书，2.邮件，3.飞书和邮件
        String sendFeiShu="1";
        String sendEmail="2";
        String sendFeiShuAndEmail="3";

        // 发送飞书消息
        if (sendFeiShu.equals(apmNotifyExecuteRecord.getNotifyWay())) {
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
        }
        // 发送邮件
        if (sendEmail.equals(apmNotifyExecuteRecord.getNotifyWay())) {
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
        }
        // 分别发送飞书和邮件消息
        if (sendFeiShuAndEmail.equals(apmNotifyExecuteRecord.getNotifyWay())) {
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
    }
    private  void removeById(Integer id) {
        apmNotifyExecuteRecordService.deleteById(id);
    }
    private  void extractedSendResultSuccess(ApmNotifyExecuteRecord apmNotifyExecuteRecord, PushSendResult sendResult) {
        if (sendResult.isSuccess()) {
            //删除已发送的记录
            if(apmNotifyExecuteRecord.getId() != null){
                removeById(apmNotifyExecuteRecord.getId());
            }
            //将数据保存到历史表
            saveHistory(apmNotifyExecuteRecord);
        } else {
            // 发送失败,记录失败日志
            log.error("发送飞书消息失败", sendResult.getMessage());
            //更新发送失败的记录
            extractedSendResultFail(apmNotifyExecuteRecord, sendResult.getMessage());
        }
    }

    /**
     * 保存发送成功的数据到历史表中
     *
     * @param apmNotifyExecuteRecord 通知执行记录
     * @date 2024/1/29 14:42
     * @author quan.cheng
     */
    private void saveHistory(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        ApmNotifyExecuteRecordHis apmNotifyExecuteRecordHis = ApmNotifyExecuteRecordHisConverter.INSTANCE.po2His(apmNotifyExecuteRecord);
        apmNotifyExecuteRecordHis.setId(null);
        apmNotifyExecuteRecordHis.setNofifyResult(1);
        apmNotifyExecuteRecordHisService.save(apmNotifyExecuteRecordHis);
    }


    private void extractedSendResultFail(ApmNotifyExecuteRecord apmNotifyExecuteRecord, String msg) {
        apmNotifyExecuteRecord.setNofifyResult(2);
        // 发送次数 在原有基础上加1
        if(apmNotifyExecuteRecord.getNofifyRetryCount() != null){
            apmNotifyExecuteRecord.setNofifyRetryCount(apmNotifyExecuteRecord.getNofifyRetryCount() + 1);
        }else{
            apmNotifyExecuteRecord.setNofifyRetryCount(1);
        }
        apmNotifyExecuteRecord.setNofifyResultMsg(msg);
        apmNotifyExecuteRecordService.save(apmNotifyExecuteRecord);
    }
}
