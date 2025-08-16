package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.mapstruct.AlmSystemFeatureDtoConverter;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.domain.object.base.ModelService;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataBatchUpdateEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataDeleteEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/28 17:32
 */
@Log4j2
@Component
public class SystemFeatureScmpUpdateEventHandler {

    @Value("${transcend.alm.update.scmp.notice.url:}")
    private String url;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private ModelService<MBaseData> modelService;

    /**
     * 不匹配判定
     *
     * @param modelCode 模型编码
     * @return 是否匹配
     */
    private static boolean nonMatch(String modelCode) {
        return !TranscendModel.matchCode(modelCode, TranscendModel.SF);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataBatchUpdateEvent event) {
        if (nonMatch(event.getModelCode())) {
            return;
        }
        event.getUpdateList().forEach(update -> {
            QueryCondition condition = QueryCondition.of().setQueries(update.getWrappers());
            List<MBaseData> mBaseData = modelService.listIncludeDelete(event.getModelCode(), condition);
            mBaseData.forEach(this::pushUpdate);
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataUpdateEvent event) {
        if (nonMatch(event.getModelCode())) {
            return;
        }
        QueryCondition condition = QueryCondition.of().setQueries(event.getWrappers());
        List<MBaseData> mBaseData = modelService.listIncludeDelete(event.getModelCode(), condition);
        mBaseData.forEach(this::pushUpdate);
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataDeleteEvent event) {
        if (nonMatch(event.getModelCode())) {
            return;
        }
        QueryCondition condition = QueryCondition.of().setQueries(event.getWrappers());
        List<MBaseData> mBaseData = modelService.listIncludeDelete(event.getModelCode(), condition);
        mBaseData.forEach(this::pushUpdate);
    }


    private void pushUpdate(MBaseData object) {
        String jsonBody = null;
        try {
            Params params = AlmSystemFeatureDtoConverter.INSTANCE.toParams(object);
            jsonBody = objectMapper.writeValueAsString(params);
            log.info("SCMP推送系统特性更新内容:{}", jsonBody);
            String result = HttpUtil.post(url, jsonBody);
            log.info("SCMP推送系统特性更新结果:{}", result);
        } catch (Exception e) {
            log.error("SCMP推送系统特性更新失败:{}", jsonBody, e);
        }
    }


    @Data
    public static class Params {
        /**
         * 特性bid
         */
        private String featureBid;
        /**
         * 特性编号
         */
        private String featureCode;
        /**
         * 特性名称
         */
        private String featureName;
        /**
         * 特性等级
         */
        private Integer level;
        /**
         * 特性SE
         */
        private String featureSe;
        /**
         * 删除标记
         */
        private Boolean deleteFlag;
        /**
         * 特性描述
         */
        private String featureDesc;
        /**
         * 责任领域
         */
        private String belongDomain;
        /**
         * 特性Owner
         */
        private String featureOwner;
        /**
         * 空间bid
         */
        private String spaceBid;
        /**
         * 空间应用bid
         */
        private String spaceAppBid;
    }

}
