package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverQueueService;
import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverSpecialConvertHolder;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.common.expression.SimpleExpression;
import com.transcend.plm.datadriven.common.tool.ModelFilterQoTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.domain.object.base.ModelService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据投递队列服务实现
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/7 11:07
 */
@Log4j2
@Service
public class AlmDataDeliverQueueServiceImpl implements AlmDataDeliverQueueService {

    private Map<String, Config> configMap;

    private final RabbitTemplate rabbitTemplate;
    private final ModelService<MBaseData> modelService;
    private final AlmDataDeliverSpecialConvertHolder converterHolder;

    public AlmDataDeliverQueueServiceImpl(RabbitTemplate rabbitTemplate,
                                          ModelService<MBaseData> domainService,
                                          AlmDataDeliverSpecialConvertHolder converterHolder) {
        this.rabbitTemplate = rabbitTemplate;
        this.modelService = domainService;
        this.converterHolder = converterHolder;
    }

    @SuppressWarnings("all")
    @Value("${transcend.alm.dataDeliverConfig:{}}")
    public void setConfig(String configsJson) {
        try (JSONValidator validator = JSONValidator.from(configsJson)) {
            if (validator.validate()) {
                List<Config> configs = JSON.parseArray(configsJson, Config.class);
                this.configMap = configs.stream()
                        .collect(Collectors.toMap(Config::getModelCode, Function.identity(), (v1, v2) -> v1));
                return;
            }
        } catch (Exception e) {
            log.error("setConfigs configsJson is not valid json", e);
        }
        log.error("setConfigs configsJson is not valid json");
    }

    @Override
    public boolean deliver(MBaseData data) {

        //1、获取配置
        Config config = Optional.ofNullable(data).map(TranscendObjectWrapper::new)
                .map(TranscendObjectWrapper::getModelCode).map(configMap::get).orElse(null);
        if (config == null) {
            return false;
        }

        //2、投递字段特殊处理
        TranscendObjectWrapper wrapper = new TranscendObjectWrapper(data);
        converterHolder.convert(wrapper);

        //3、确认投递的字段
        String[] fields = getFields(config, data);
        if (ArrayUtils.isEmpty(fields)) {
            return false;
        }

        //4、过滤投递的字段
        Map<String, Object> deliverObject = getDeliverObject(fields, data);
        if (deliverObject.isEmpty()) {
            return false;
        }

        //5、投递到队列
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, wrapper.getModelCode(), deliverObject, message -> {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setMessageId(wrapper.getBid()); // 设置自定义的 messageId
                return message;
            });

            return true;
        } catch (Exception e) {
            log.error("deliver fail , data:{}", data, e);
        }
        return false;
    }

    @Override
    public long deliver(List<MBaseData> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return 0;
        }
        return dataList.stream().filter(this::deliver).count();
    }


    @Override
    public long deliverByCondition(String modelCode, List<QueryWrapper> wrappers) {
        if (notSupport(modelCode)) {
            return 0;
        }

        long result = 0;
        int size = 100;
        Object lastId = null;

        List<MBaseData> list;
        do {
            //添加上次处理到的数据ID，防止重复
            List<QueryWrapper> queries = wrappers;
            if (lastId != null) {
                QueryWrapper wrapper = new QueryWrapper();
                wrapper.lt("id", lastId);
                queries = QueryConveterTool.appendMoreQueriesAndCondition(wrappers, QueryWrapper.buildSqlQo(wrapper));
            }

            QueryCondition condition = QueryCondition.of().setQueries(queries)
                    .setAdditionalSql(" TRUE ORDER BY id DESC LIMIT " + size);

            list = modelService.listIncludeDelete(modelCode, condition);

            if (!list.isEmpty()) {
                lastId = list.get(list.size() - 1).get("id");
                result += deliver(list);
            }

            //如果查询数量等于查询上限条数，则表面可能还有数据，则继续查询
        } while (list.size() >= size);

        return result;
    }


    @Override
    @Async
    public synchronized void compensateDeliver(String modelCode, ModelMixQo params) {
        if (StringUtils.isBlank(modelCode)) {
            return;
        }

        QueryCondition condition = QueryCondition.of();
        if (params != null) {
            ModelFilterQoTools.analysis(params.getQueries());
            condition.setQueries(QueryConveterTool.convert(params.getQueries(), params.getAnyMatch(), false))
                    .setOrders(params.getOrders())
                    .setPage(params.getPageSize(), params.getPageCurrent());
        }

        String[] modelCodes = modelCode.split(",");
        if (modelCodes.length > 1) {
            for (String code : modelCodes) {
                try {
                    if (notSupport(code)) {
                        return;
                    }
                    long count = deliverByCondition(code, condition.getQueries());
                    log.info("compensateDeliver modelCode:{} count:{}", code, count);
                } catch (Exception e) {
                    log.error("compensateDeliver modelCode:{} error", code, e);
                }
            }
        }

    }


    //region private method

    /**
     * 不支持投递
     *
     * @param modelCode 模型编码
     * @return true 不支持投递，false 支持投递
     */
    private boolean notSupport(String modelCode) {
        if (StringUtils.isBlank(modelCode)) {
            return true;
        }
        return !this.configMap.containsKey(modelCode);
    }

    /**
     * 获取投递的字段
     *
     * @param config 配置
     * @param data   投递实例数据
     * @return 可以投递的字段
     */
    private String[] getFields(Config config, MBaseData data) {

        //获取配置字段
        String[] fields = Optional.ofNullable(config.getConditionFields())
                .flatMap(conditionFields ->
                        conditionFields.stream().filter(Objects::nonNull).filter(params -> {
                            //对条件进行匹配
                            List<Expression> expressions = params.getExpressions();
                            if (CollUtil.isEmpty(expressions)) {
                                return false;
                            }
                            //转换为简单表达式
                            List<SimpleExpression> expressionList = expressions.stream().map(expression -> {
                                Object instanceValue = data.get(expression.getFieldName());
                                return SimpleExpression.of(instanceValue, expression.getCondition(), expression.getValue());
                            }).collect(Collectors.toList());

                            //执行条件匹配
                            return SimpleExpression.evaluateExpressions(expressionList, params.isAnyMatch());
                        }).findFirst().map(ConditionField::getFields)
                ).orElseGet(config::getFields);

        if (ArrayUtils.isEmpty(fields)) {
            return DEFAULT_FIELDS;
        }

        //合并默认字段
        return Stream.concat(Arrays.stream(DEFAULT_FIELDS), Arrays.stream(fields)).distinct().toArray(String[]::new);
    }

    /**
     * 获取投递对象
     *
     * @param fields 字段
     * @param data   投递对象
     * @return 投递对象
     */
    @NotNull
    private Map<String, Object> getDeliverObject(String[] fields, MBaseData data) {
        Map<String, Object> deliverObject = new HashMap<>(fields.length);
        for (String field : fields) {
            Object value = data.get(field);
            if (value != null) {
                deliverObject.put(field, dataConvert(value));
            }
        }
        return deliverObject;
    }

    /**
     * 数据转换
     *
     * @param value 数据
     * @return 转换后的数据
     */
    private Object dataConvert(Object value) {
        if (value instanceof LocalDateTime) {
            return DateUtil.format((LocalDateTime) value, "yyyy-MM-dd HH:mm:ss");
        } else if (value instanceof Date) {
            return DateUtil.format((Date) value, "yyyy-MM-dd HH:mm:ss");
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        return value;
    }

    //endregion


}
