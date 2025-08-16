package com.transcend.plm.datadriven.apm.extension.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.transcend.plm.configcenter.api.model.view.dto.RelationInfo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.extension.service.XpmKpiLogicService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 实现
 * @date 2024/04/23 13:54
 **/
@Component
@Slf4j
@DependsOn("modelCodeProperties")
public class XpmKpiLogicServiceImpl implements XpmKpiLogicService {
    private static final String DETAIL_RECORD = "%s %s %s %s";
    private final String SCORE_OPERATION_TIME = "jiajianDate";
    private final String SCORE = "jiaJianScore";
    private final String SCORE_OPERATION = "isJiaJianFen";
    private final String SCORE_OPERATION_TYPE = "jiajianDesc";
//    private final String SOURCE_MODEL_CODE = "A4Y";

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelStandardI;

    @Resource
    private IAppDataService appDataService;


    private final Map<String, ScoreDetail> scoreDetailMap = ImmutableMap.of(
            KPI_INFO_SCHEDULE_REL_MODEL_CODE,new ScoreDetail("progressDimension","progressDetails",BigDecimal.valueOf(25))
            ,KPI_INFO_QUALITY_REL_MODEL_CODE,new ScoreDetail("qualityDimension","qualityDetails",BigDecimal.valueOf(25))
            ,KPI_INFO_BEHAVIOR_REL_MODEL_CODE,new ScoreDetail("behavioralDimension","behaviorDetails",BigDecimal.valueOf(10))
            ,KPI_INFO_VALUE_REL_MODEL_CODE,new ScoreDetail("valuePointDimension","valueDetail",BigDecimal.valueOf(40)));
    private final Map<String,String> targetAndRelationMap = ImmutableMap.of(
            KPI_SCHEDULE_MODEL_CODE,KPI_INFO_SCHEDULE_REL_MODEL_CODE
            ,KPI_QUALITY_MODEL_CODE,KPI_INFO_QUALITY_REL_MODEL_CODE
            ,KPI_BEHAVIOR_MODEL_CODE,KPI_INFO_BEHAVIOR_REL_MODEL_CODE
            ,KPI_VALUE_P_MODEL_CODE,KPI_INFO_VALUE_REL_MODEL_CODE);
    @Override
    public void collectItemScore(RelationInfo relationInfo, String sourceBid) {
        final RelationMObject relationQuery = RelationMObject.builder().sourceBid(sourceBid)
                .targetModelCode(relationInfo.getTargetModelCode()).sourceModelCode(relationInfo.getSourceModelCode())
                .relationModelCode(relationInfo.getModelCode()).build();
        List<MObject> targetList =  objectModelStandardI.listRelationMObjects(relationQuery);
        log.info("查询源对象sourceBid:{},modelCode:{}",sourceBid,relationInfo.getModelCode());
        MObject sourceIns = objectModelStandardI.getByBid(relationInfo.getSourceModelCode(),sourceBid);
        if(sourceIns == null){
            log.error("sourceIns is null,sourceBid:{}",sourceBid);
            throw new BusinessException("sourceIns is null");
        }
        if(CollectionUtils.isNotEmpty(targetList)){
            BigDecimal totalScore = BigDecimal.ZERO;
            CalculateResult calculateResult = getScoreDetail(targetList, totalScore);
            StringJoiner stringJoiner = new StringJoiner("\n");
            calculateResult.getScoreDetail().forEach(stringJoiner::add);
            BigDecimal score = BigDecimal.valueOf(100).add(calculateResult.getScore());
            sourceIns.put(scoreDetailMap.get(relationInfo.getModelCode()).score, score.compareTo(BigDecimal.ZERO) < 0 ?BigDecimal.ZERO:score.stripTrailingZeros().toPlainString());
            sourceIns.put(scoreDetailMap.get(relationInfo.getModelCode()).scoreDetail,stringJoiner.toString());
            calculateTotalScore(sourceIns);
        }else {
            sourceIns.put(scoreDetailMap.get(relationInfo.getModelCode()).scoreDetail,"");
            calculateTotalScore(sourceIns);
        }
        log.info("更新KPI属性数据:{},bid:{},sourceModelCode:{}",JSON.toJSONString(sourceIns),sourceBid,relationInfo.getSourceModelCode());
        if(appDataService.updateByBid(relationInfo.getSourceModelCode(),sourceBid, sourceIns)){
            log.error("更新KPI属性数据失败");
        }
    }

    @Override
    public void updateInsThenIsCollect(String modelCode, String bid, MSpaceAppData mSpaceAppData) {
        if(!mSpaceAppData.containsKey(SCORE)
                && !mSpaceAppData.containsKey(SCORE_OPERATION)
                && !mSpaceAppData.containsKey(SCORE_OPERATION_TIME)){
            return;
        }
        String relationCode = targetAndRelationMap.get(modelCode);
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.TARGET_BID.getColumn(), bid).and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        log.info("queryWrappers:{}",queryWrappers);
        List<MObject> list = objectModelStandardI.list(relationCode, queryWrappers);
        log.info("list:{}",list);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        MObject mObject= list.get(0);
        RelationInfo relationInfo = new RelationInfo();
        relationInfo.setModelCode(relationCode);
        relationInfo.setSourceModelCode(KPI_INFO_MODEL_CODE);
        relationInfo.setTargetModelCode(modelCode);
        collectItemScore(relationInfo,String.valueOf(mObject.get(RelationEnum.SOURCE_BID.getCode())));
    }

    /**
     * 获取得分明细
     * @param targetList
     * @return
     */
    private CalculateResult getScoreDetail(List<MObject> targetList, BigDecimal totalScore) {
        List<String> scoreDetail = Lists.newArrayList();
        for(MObject target:targetList){
            BigDecimal score = new BigDecimal(target.get(SCORE) == null? "0" : target.get(SCORE).toString());
            String scoreOperationTime = target.get(SCORE_OPERATION_TIME).toString();
            scoreOperationTime = scoreOperationTime.length()>=10?scoreOperationTime.substring(0,10):scoreOperationTime;
            String scoreOperation = target.get(SCORE_OPERATION).toString();
            if("jiafen".equals(scoreOperation)){
             totalScore =  totalScore.add(score);
                scoreDetail.add(String.format(DETAIL_RECORD, scoreOperationTime, "加分", score.stripTrailingZeros().toPlainString(),target.get(SCORE_OPERATION_TYPE)));
            }else if("jianfen".equals(scoreOperation)){
                totalScore = totalScore.subtract(score);
                scoreDetail.add(String.format(DETAIL_RECORD, scoreOperationTime, "减分", score.stripTrailingZeros().toPlainString(),target.get(SCORE_OPERATION_TYPE)));
            }
        }
        return  new CalculateResult(totalScore,scoreDetail);
    }

    /**
     * 计算kpi总得分
     * @param insData
     */
    private void calculateTotalScore(MObject insData) {
        BigDecimal totalScore = new BigDecimal(0);
        log.info("开始计算kpi总得分{}", JSON.toJSONString(insData));
        for(ScoreDetail scoreDetail : scoreDetailMap.values()) {
            String detailScore = insData.get(scoreDetail.getScore()) == null ? "0" : insData.get(scoreDetail.getScore()).toString();
            log.info("score{}", detailScore);
            BigDecimal score = new BigDecimal("null".equals(detailScore) || StringUtils.isBlank(detailScore) ? "0" : detailScore);
            BigDecimal rate = scoreDetail.getRate().divide(new BigDecimal(100));
            totalScore = totalScore.add(score.multiply(rate));
        }
        insData.put("kpiTotalScore", totalScore.setScale(2,BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros().toPlainString());
    }

    class ScoreDetail {
        private String score;
        private String scoreDetail;
        private BigDecimal rate;

        ScoreDetail(String score, String scoreDetail, BigDecimal rate) {
            this.score = score;
            this.scoreDetail = scoreDetail;
            this.rate = rate;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getScoreDetail() {
            return scoreDetail;
        }

        public void setScoreDetail(String scoreDetail) {
            this.scoreDetail = scoreDetail;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public void setRate(BigDecimal rate) {
            this.rate = rate;
        }
    }
    class CalculateResult {
        private BigDecimal score;
        private List<String> scoreDetail;
        CalculateResult(BigDecimal score, List<String> scoreDetail) {
            this.score = score;
            this.scoreDetail = scoreDetail;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }

        public List<String> getScoreDetail() {
            return scoreDetail;
        }

        public void setScoreDetail(List<String> scoreDetail) {
            this.scoreDetail = scoreDetail;
        }
    }
}
