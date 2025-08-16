package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.NumberUtil;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataAddEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataBatchAddEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 生成编码处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/24 16:21
 */
@Slf4j
@Component
@AllArgsConstructor
public class GenerateCodeAddEventHandler {
    private final ObjectModelStandardI<MObject> objectModelStandardI;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String DELIMITER = "-";
    private static final String CODE_FIELD_NAME = "coding";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMM");

    @EventListener
    public void handle(BaseDataAddEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }

        TranscendObjectWrapper data = new TranscendObjectWrapper(event.getData());
        //已有编码的数据不生成编码
        if (StringUtils.isNotBlank(data.getStr(CODE_FIELD_NAME))) {
            return;
        }
        data.put(CODE_FIELD_NAME, generateCode(modelCode));

    }

    @EventListener
    public void handle(BaseDataBatchAddEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }
        event.getDataList().stream().map(TranscendObjectWrapper::new)
                // 已有编码的数据不生成编码
                .filter(data -> StringUtils.isBlank(data.getStr(CODE_FIELD_NAME)))
                .forEach(data -> data.put(CODE_FIELD_NAME, generateCode(getPrefix(modelCode))));
    }

    /**
     * 获取前缀方法
     *
     * @param modelCode 模型编码
     * @return 获取到前缀
     */
    String getPrefix(String modelCode) {
        TranscendModel transcendModel = TranscendModel.fromCode(modelCode);
        Assert.notNull(transcendModel, "modelCode is not match");
        String date = DATE_FORMAT.format(DateUtil.date());
        return transcendModel.name() + DELIMITER + date;
    }

    /**
     * 生成编码方法
     *
     * @param modelCode 模型编码
     * @return 生成编码
     */
    String generateCode(String modelCode) {
        String prefix = getPrefix(modelCode);
        Long sequence = getSequence(modelCode, prefix);
        return String.format("%s%s%06d", prefix, DELIMITER, sequence);
    }


    /**
     * 获取序列
     *
     * @param modelCode 模型编码
     * @param prefix    序列前缀
     * @return 生成序列值
     */
    Long getSequence(String modelCode, String prefix) {
        String key = "alm:code:sequence:" + prefix;

        //初始化key
        if (!redisTemplate.hasKey(key)) {
            //获取序列种子
            int sequenceSeed = getSequenceSeed(modelCode, prefix);
            redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(sequenceSeed));
            redisTemplate.expire(key, 31, TimeUnit.DAYS);
        }
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 获取序列种子
     *
     * @param modelCode 模型编码
     * @param prefix    序列前缀
     * @return 序列种子
     */
    private int getSequenceSeed(String modelCode, String prefix) {
        int prefixDataCount = prefixDataCount(modelCode, prefix);
        int prefixMaxSequence = prefixMaxSequence(modelCode, prefix);
        return Math.max(prefixDataCount, prefixMaxSequence);
    }

    /**
     * 获取前缀最大序列
     *
     * @param modelCode 模型编码
     * @param prefix    前缀
     * @return 前缀最大序列
     */
    private int prefixMaxSequence(String modelCode, String prefix) {
        QueryWrapper wrapper = new QueryWrapper().rLike(CODE_FIELD_NAME, prefix);
        QueryCondition condition = QueryCondition.of().setQueries(QueryWrapper.buildSqlQo(wrapper))
                .setAdditionalSql(" TRUE ORDER BY " + NamingCase.toUnderlineCase(CODE_FIELD_NAME) + " DESC LIMIT 1");
        List<MObject> queryList = objectModelStandardI.list(modelCode, condition);
        return Optional.ofNullable(queryList)
                //获取第一个数据
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                //获取编码
                .map(MObject::getCoding).filter(StringUtils::isNotBlank)
                //截取尾数序列
                .map(coding -> StringUtils.substringAfterLast(coding, DELIMITER))
                //转换为数字
                .filter(NumberUtil::isInteger).map(Integer::parseInt)
                .orElse(0);
    }

    /**
     * 获取前缀数据数量
     *
     * @param modelCode 模型编码
     * @param prefix    前缀
     * @return 前缀数据数量
     */
    private int prefixDataCount(String modelCode, String prefix) {
        QueryWrapper wrapper = new QueryWrapper().rLike(CODE_FIELD_NAME, prefix);
        return objectModelStandardI.count(modelCode, QueryWrapper.buildSqlQo(wrapper));
    }

    /**
     * 是否匹配
     *
     * @param modelCode 模型编码
     * @return 是否匹配
     */
    private static boolean nonMatch(String modelCode) {
        return !TranscendModel.matchCode(modelCode,
                TranscendModel.RR, TranscendModel.IR, TranscendModel.SR, TranscendModel.AR,
                TranscendModel.SF);
    }

}
