package com.transcend.plm.datadriven.apm.api.service;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@Slf4j
@Component
public class UserDemandService {

    @Resource
    private IApmSpaceAppDataDrivenService iApmSpaceAppDataService;

    @Value("${transcend.datadriven.apm.tones.file.url: http://10.150.98.136:8501/tones-component/tones-component/file/getFiles?id=}")
    private String tonesFileUrl;

    @ApiOperation("批量新增")
    public Boolean batchAdd(List<Map<String, Object>> dataList) {
        if (Objects.isNull(dataList)) {
            throw new PlmBizException("参数不能为空");
        }
        List<MSpaceAppData> mSpaceAppDataList = dataList.stream().map(this::convertDataResult
        ).collect(Collectors.toList());
        return iApmSpaceAppDataService.addBatch(
                getUserDemandSpace(), getUserDemandSpaceApp(), mSpaceAppDataList, null);
    }

    @ApiOperation("新增")
    public Boolean add(Map<String, Object> param) {
        if (Objects.isNull(param)) {
            throw new PlmBizException("参数不能为空");
        }
        return handleDataResult(false, param);
    }

    @ApiOperation("修改")
    public Boolean update(Map<String, Object> param) {
        if (Objects.isNull(param)) {
            throw new PlmBizException("参数不能为空");
        }
        return handleDataResult(true, param);
    }

    /**
     * todo
     *
     * @param
     * @return
     */
    @ApiOperation("批量逻辑删除")
    public Boolean batchLogicalDeleteDelete(List<String> bids) {
        if (CollectionUtils.isEmpty(bids)) {
            throw new PlmBizException("参数不能为空");
        }
        return iApmSpaceAppDataService.batchLogicalDeleteByBids(getUserDemandSpaceApp(), bids);
    }

    @NotNull
    private static String getUserDemandSpaceApp() {
        return "USER_DEMAND_SPACE_APP";
    }

    @NotNull
    private static String getUserDemandSpace() {
        return "USER_DEMAND_SPACE";
    }


    @NotNull
    private Boolean handleDataResult(boolean isEdit, Map<String, Object> param) {
        MSpaceAppData mSpaceAppData = convertDataResult(param);
        String bid = String.valueOf(mSpaceAppData.getBid());
        return isEdit ?
                iApmSpaceAppDataService.updatePartialContent(getUserDemandSpaceApp(), bid, mSpaceAppData) :
                iApmSpaceAppDataService.add(getUserDemandSpaceApp(), mSpaceAppData) != null;
    }

    private MSpaceAppData convertDataResult(Map<String, Object> param) {
        MSpaceAppData data = new MSpaceAppData();
        data.putAll(param);
        // 参考飞书：https://transsioner.feishu.cn/wiki/LEeewcE5iiJntAkDZNxcJzTanme
        // 1.TONES传递过来的是id（long）
        Object bid = data.get(TranscendModelBaseFields.BID);
        if (bid == null) {
            throw new PlmBizException("bid不能为空");
        }
        data.remove("id");
        // 2.TONES传递过来的| JIRA编号 | bugUrl | => jiraCode
        Object jiraCode = data.get("bugUrl");
        if (jiraCode != null) {
            data.put("jiraCode", jiraCode);
            data.remove("bugUrl");
        }

        // 3.channel=>sourceChannel
        // 3.来源渠道

        /**
         * 来源渠道的枚举值
         * FAN_TRY(1, "研发出海"),
         *  FAN_OPERATION(2, "粉丝试用"),
         *  USE_RESEARCH(3, "电商论坛社媒"),
         *     BUSINESS_DIVISION(4, "用户社群"),
         *     MOBILE_INTERNET(5, "用户调研"),
         *     FEED_BACK(6, "售后"),
         *     GOOGLE(7, "地区部"),
         *     KT8(8, "事业部"),
         *     VIP_FAN(9, "Google");
         */
        Map<String, String> channelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        channelMap.put("1", "研发出海");
        channelMap.put("2", "粉丝试用");
        channelMap.put("3", "电商论坛社媒");
        channelMap.put("4", "用户社群");
        channelMap.put("5", "用户调研");
        channelMap.put("6", "售后");
        channelMap.put("7", "地区部");
        channelMap.put("8", "事业部");
        channelMap.put("9", "Google");
        Object sourceChannel = param.get("channel");
        if (sourceChannel != null) {
            // tones 新增的时候是Integer类型，修改的时候是String类型
            data.put("sourceChannel", channelMap.get(sourceChannel.toString()));
            data.remove("channel");
        }
        // 4.state=>lifeCycleCode
        /**
         * INIT(0, "已创建"),
         *  ESTIMATING(2, "评估中"), UNDER_EVALUATION
         *  DESIGNING(3, "设计中"),  DESIGNER  UNDER_DESIGN
         *  DEVELOPING(4, "研发中"),
         *  VERIFIED(5, "待验证"),    TO_BE_VERIFIED
         *  FINISHED(7, "已完成"),    COMPLETED
         *  CLOSED(8, "已关闭"),      CLOSED
         *  REJECTED(9, "已驳回"),
         * VERIFY_REJECTED(10, "已拒绝"),
         *  DEFERRED_ASSESSMENT(11,"延后评估");
         */
        Map<String, String> stateMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        stateMap.put("0", "INIT");
        stateMap.put("2", "ESTIMATING");
        stateMap.put("3", "DESIGNING");
        stateMap.put("4", "DEVELOPING");
        stateMap.put("5", "VERIFIED");
        stateMap.put("7", "FINISHED");
        stateMap.put("8", "CLOSED");
        stateMap.put("9", "REJECTED");
        stateMap.put("10", "VERIFY_REJECTED");
        stateMap.put("11", "DEFERRED_ASSESSMENT");
        Object status = param.get("status");
        if (status != null) {
            data.put(TranscendModelBaseFields.LIFE_CYCLE_CODE, stateMap.get(status.toString()));
            data.remove("status");
        }
        // 5.attachmentVoList=>files
        // TONES传递过来的附件 attachmentVoList = []
        /**
         * TONES的数据
         * [{
         *                 "uid": "1001783",
         *                 "id": 1001783,
         *                 "name": "v.jpg",
         *                 "status": "done",
         *                 "url": "2ea1d24f0287487cb4718d67f737ecae_v.jpg",
         *                 "path": "2ea1d24f0287487cb4718d67f737ecae_v.jpg",
         *                 "size": 14818
         *         }]
         * // TONES 需要拼接：
         * http://10.150.98.136:8501/tones-component/tones-component/file/getFiles?id=8cf7b34085984ff9917c2d4e8b573e09_v.jpg
         * transcend的数据
         * [ {
         *       "name" : "bug文档0727.doc",
         *       "url" : "https://transsion-platform02.oss-cn-shenzhen.aliyuncs.com/ipm/doc/files/1698722428249/bug%E6%96%87%E6%A1%A30727.doc"
         *     } ]
         */
        Object attachmentVoList4Object = data.get("attachmentVoList");
        if (attachmentVoList4Object instanceof List) {
            List<Map<String, Object>> listMap = (List<Map<String, Object>>) attachmentVoList4Object;
            listMap.forEach(stringObjectMap ->
                    stringObjectMap.put("url", tonesFileUrl + stringObjectMap.get("url"))
            );
            data.put("files", attachmentVoList4Object);
        }
        data.remove("attachmentVoList");
        // 6.优先级
        /**
         * P0(0, "P0"),
         * P1(1, "P1"),
         * P2(2, "P2"),
         * P3(3, "P3");
         */

        Object priority = data.get("priority");
        if (priority != null) {
            Map<String, String> priortyMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            priortyMap.put("0", "P0");
            priortyMap.put("1", "P1");
            priortyMap.put("2", "P2");
            priortyMap.put("3", "P3");
            data.put("priority", priortyMap.get(priority.toString()));
        }

        // 7.计划完成时间 expectedFinishDate = plannedCompletionTime
        Object expectedFinishDate = data.get("expectedFinishDate");
        // Tue Nov 14 10:20:29 CST 2023
        if (expectedFinishDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            try {
                Date date = format.parse(expectedFinishDate.toString());
                data.put("plannedCompletionTime", date);
            } catch (ParseException e) {
                log.error("消费Tones用户需求plannedCompletionTime时间转换异常", e);
            }
            data.remove("expectedFinishDate");
        }
        // 8.原始提出人 originSubmitter=>originalProposer
        Object originSubmitter = data.get("originSubmitter");
        if (originSubmitter != null) {
            data.put("originalProposer", originSubmitter);
            data.remove("originSubmitter");
        }
        // 9.提报人 reportedBy=> reportedBy 格式：姓名（工号） submitter=>proposer
        String regex = "\\((\\d+)\\)";
        Object reportedBy = data.get("reportedBy");
        if (reportedBy != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(reportedBy.toString());
            if (matcher.find()) {
                String number = matcher.group(0);
                reportedBy = number.substring(1, number.length() - 1);
            }
            data.put("reportedBy", reportedBy);
        }
        // 产品经理 owner=>productOwner 同提报人 owner -> 石涵(18649508),万伟志(18652121),王永强(18651741)
        Object productManager = data.get(TranscendModelBaseFields.OWNER);
        if (productManager != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(productManager.toString());
            List<String> productManagerList = Lists.newArrayList();
            while (matcher.find()) {
                String number = matcher.group(1);
                productManagerList.add(number);
            }
            data.put("productOwner", productManagerList);
            data.remove(TranscendModelBaseFields.OWNER);
        }
        //        采纳人 approver
        Object approver = data.get("approver");
        if (approver != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(approver.toString());
            if (matcher.find()) {
                String number = matcher.group(0);
                approver = number.substring(1, number.length() - 1);
            }
            data.put("approver", approver);
        }
        //        采纳时间 evaluateTime
        Object evaluateTime = data.get("evaluateTime");
        // Tue Nov 14 10:20:29 CST 2023
        if (evaluateTime != null) {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            try {
                Date date = format.parse(expectedFinishDate.toString());
                data.put("evaluateTime", date);
            } catch (ParseException e) {
                log.error("消费Tones用户需求plannedCompletionTime时间转换异常", e);
            }
            data.remove("expectedFinishDate");
        }
        //        评估结果 acceptState  => evaluationResult
        //                DEFAULT(0, "已创建"),
        //                ACCEPT(1, "已采纳"),
        //                REJECT(2, "已驳回"),
        //                REFUSED(3, "已拒绝"),
        //                REPEAT(4, "重复")
        Object acceptState = data.get("acceptState");
        if (priority != null) {
            Map<String, String> priortyMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            priortyMap.put("0", "已创建");
            priortyMap.put("1", "已采纳");
            priortyMap.put("2", "已驳回");
            priortyMap.put("3", "已拒绝");
            priortyMap.put("4", "重复");
            data.put("evaluationResult", priortyMap.get(acceptState.toString()));
        }

        return data;
    }

//    public static void main(String[] args) {
//        String dateString = "Tue Nov 14 10:20:29 CST 2023";
//        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
//
//        try {
//            Date date = format.parse(dateString);
//            System.out.println(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }


}
